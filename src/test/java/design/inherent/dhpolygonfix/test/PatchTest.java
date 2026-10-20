package design.inherent.dhpolygonfix.test;

import com.seibel.distanthorizons.lwjgl.ILWJGLService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.MixinService;
import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.opengl.GL11.*;

public final class PatchTest {
    static final String TARGET = "com.seibel.distanthorizons.common.render.openGl.glObject.GLState";

    static byte[] transform() throws Exception {
        MixinBootstrap.init();
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
        Mixins.addConfiguration("dh_polygon_fix.mixins.json");
        TestMixinService service = (TestMixinService) MixinService.getService();
        byte[] original;
        try (var in = service.getResourceAsStream(TARGET.replace('.', '/') + ".class")) {
            original = in.readAllBytes();
        }
        byte[] patched = service.transformer().transformClassBytes(TARGET, TARGET, original);
        Files.createDirectories(Path.of("build/verification"));
        Files.write(Path.of("build/verification/GLState-original.class"), original);
        Files.write(Path.of("build/verification/GLState-patched.class"), patched);

        ClassNode before = read(original), after = read(patched);
        MethodNode oldSave = method(before, "saveState"), newSave = method(after, "saveState");
        assertEquals(calls(oldSave, "glGetInteger") - 1, calls(newSave, "glGetInteger"));
        assertEquals(1, calls(newSave, "dhPolygonFix$queryPolygonModes"));
        // Verify every unrelated method remains bytecode-identical.
        for (MethodNode previous : before.methods) {
            if (previous.name.equals("saveState")) continue;
            MethodNode current = after.methods.stream()
                .filter(m -> m.name.equals(previous.name) && m.desc.equals(previous.desc)).findFirst().orElseThrow();
            assertArrayEquals(methodBytes(previous), methodBytes(current), previous.name);
        }
        return patched;
    }

    static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }
    static MethodNode method(ClassNode node, String suffix) {
        return node.methods.stream().filter(m -> m.name.endsWith(suffix)).findFirst().orElseThrow();
    }
    private static long calls(MethodNode method, String suffix) {
        return Arrays.stream(method.instructions.toArray())
            .filter(i -> i instanceof MethodInsnNode m && m.name.endsWith(suffix)).count();
    }
    private static byte[] methodBytes(MethodNode method) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "Compare", null, "java/lang/Object", null);
        method.accept(writer);
        writer.visitEnd();
        return writer.toByteArray();
    }

    /** Extract only the actually transformed handler for execution without Minecraft initialization. */
    static Method executableHandler(byte[] patched) throws Exception {
        ClassNode node = read(patched);
        MethodNode handler = method(node, "dhPolygonFix$queryPolygonModes");
        // The original instance handler does not use 'this'. Make it a static method,
        // shifting parameter/local slots down by one. Instructions otherwise remain intact.
        handler.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
        handler.name = "query";
        for (AbstractInsnNode instruction : handler.instructions) {
            if (instruction instanceof VarInsnNode variable) variable.var--;
            if (instruction instanceof IincInsnNode increment) increment.var--;
        }
        handler.localVariables = null;
        for (AbstractInsnNode instruction : handler.instructions.toArray()) {
            if (instruction instanceof FrameNode) handler.instructions.remove(instruction);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "ExtractedHandler", null, "java/lang/Object", null);
        handler.accept(writer);
        writer.visitEnd();
        class Loader extends ClassLoader {
            Class<?> define(byte[] bytes) { return defineClass("ExtractedHandler", bytes, 0, bytes.length); }
        }
        return new Loader().define(writer.toByteArray()).getMethod("query", ILWJGLService.class, int.class);
    }

    @Test
    void realMixinChangesOnlyOneQueryAndHandlesOneOrTwoResults() throws Exception {
        Method handler = executableHandler(transform());
        for (int count : new int[] {1, 2}) {
            ILWJGLService service = (ILWJGLService) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] {ILWJGLService.class},
                (proxy, method, args) -> {
                    assertEquals("glGetIntegerv", method.getName(), "scalar query must never execute");
                    assertEquals(GL_POLYGON_MODE, args[0]);
                    int[] values = (int[]) args[1];
                    assertEquals(2, values.length);
                    values[0] = GL_LINE;
                    if (count == 2) values[1] = GL_FILL; // Preserve first-value semantics.
                    return null;
                });
            assertEquals(GL_LINE, handler.invoke(null, service, GL_POLYGON_MODE));
            InvocationTargetException error = assertThrows(InvocationTargetException.class,
                () -> handler.invoke(null, service, GL_CULL_FACE_MODE));
            assertInstanceOf(IllegalStateException.class, error.getCause());
        }
    }
}
