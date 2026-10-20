package design.inherent.dhpolygonfix.test;

import com.seibel.distanthorizons.lwjgl.ILWJGLService;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.IntBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.junit.jupiter.api.Assertions.*;

/** Requires a graphical session. Exercises the real transformed handler on the host driver. */
public final class NativeProbe {
    public static void main(String[] args) throws Exception {
        Method handler = PatchTest.executableHandler(PatchTest.transform());
        GLFWErrorCallback callback = GLFWErrorCallback.createPrint(System.err);
        callback.set();
        if (!glfwInit()) throw new IllegalStateException("GLFW initialization failed");
        long window = 0;
        try {
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
            window = glfwCreateWindow(64, 64, "DH patch verification", 0, 0);
            if (window == 0) throw new IllegalStateException("OpenGL context creation failed");
            glfwMakeContextCurrent(window);
            GL.createCapabilities();
            System.out.println("GL=" + glGetString(GL_VERSION));
            System.out.println("Renderer=" + glGetString(GL_RENDERER));
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                // Reserve the adjacent word so the buggy scalar call cannot corrupt
                // unowned memory: a second result lands in this allocated canary.
                IntBuffer canary = stack.ints(0x12345678);
                assertEquals(GL_FILL, glGetInteger(GL_POLYGON_MODE));
                System.out.printf("Original scalar adjacent word=0x%X%n", canary.get(0));
                assertTrue(canary.get(0) == GL_FILL || canary.get(0) == 0x12345678);
            }
            ILWJGLService service = (ILWJGLService) Proxy.newProxyInstance(
                NativeProbe.class.getClassLoader(), new Class<?>[] {ILWJGLService.class},
                (proxy, method, arguments) -> {
                    assertEquals("glGetIntegerv", method.getName());
                    int[] output = (int[]) arguments[1];
                    assertEquals(2, output.length);
                    glGetIntegerv((Integer) arguments[0], output);
                    return null;
                });
            for (int i = 0; i < 10000; i++) {
                int expected = i % 2 == 0 ? GL_FILL : GL_LINE;
                glPolygonMode(GL_FRONT_AND_BACK, expected);
                int saved = (Integer) handler.invoke(null, service, GL_POLYGON_MODE);
                assertEquals(expected, saved);
                glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
                glPolygonMode(GL_FRONT_AND_BACK, saved); // Same restore semantics as DH.
                int[] actual = new int[2];
                glGetIntegerv(GL_POLYGON_MODE, actual);
                assertEquals(expected, actual[0]);
                assertEquals(GL_NO_ERROR, glGetError());
            }
            System.out.println("PASS: 10000 transformed-handler query/restore cycles; no GL errors");
        } finally {
            if (window != 0) glfwDestroyWindow(window);
            glfwTerminate();
            callback.free();
        }
    }
}
