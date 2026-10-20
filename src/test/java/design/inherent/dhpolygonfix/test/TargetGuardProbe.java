package design.inherent.dhpolygonfix.test;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.MixinService;
import static org.junit.jupiter.api.Assertions.*;

/** Real transformer must reject target drift rather than leave a partial patch. */
public final class TargetGuardProbe {
    public static void main(String[] args) throws Exception {
        assertEquals(1, args.length);
        String mutation = args[0];
        assertTrue(mutation.equals("missing") || mutation.equals("duplicate"));
        MixinBootstrap.init();
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
        Mixins.addConfiguration("dh_polygon_fix.mixins.json");
        TestMixinService service = (TestMixinService) MixinService.getService();
        byte[] original;
        try (var in = service.getResourceAsStream(PatchTest.TARGET.replace('.', '/') + ".class")) {
            original = in.readAllBytes();
        }
        ClassNode node = PatchTest.read(original);
        MethodNode save = PatchTest.method(node, "saveState");
        MethodInsnNode query = null;
        boolean afterCull = false;
        int matches = 0;
        for (AbstractInsnNode instruction : save.instructions) {
            if (instruction instanceof FieldInsnNode field
                    && field.name.equals("cullMode") && field.getOpcode() == Opcodes.PUTFIELD) {
                afterCull = true;
            }
            if (afterCull && instruction instanceof MethodInsnNode call
                    && call.owner.equals("com/seibel/distanthorizons/lwjgl/ILWJGLService")
                    && call.name.equals("glGetInteger") && call.desc.equals("(I)I")) {
                query = call;
                matches++;
            }
        }
        assertEquals(1, matches, "fixture must identify the original polygon query");
        InsnList replacement = new InsnList();
        if (mutation.equals("missing")) {
            // Consume the service and pname, then supply an integer result.
            replacement.add(new InsnNode(Opcodes.POP2));
            replacement.add(new InsnNode(Opcodes.ICONST_0));
            save.instructions.insertBefore(query, replacement);
            save.instructions.remove(query);
        } else {
            // Add a second valid query with the same receiver and pname.
            replacement.add(new InsnNode(Opcodes.DUP2));
            replacement.add(new MethodInsnNode(query.getOpcode(), query.owner,
                query.name, query.desc, query.itf));
            replacement.add(new InsnNode(Opcodes.POP));
            save.instructions.insertBefore(query, replacement);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Throwable error = assertThrows(Throwable.class, () -> service.transformer()
            .transformClassBytes(PatchTest.TARGET, PatchTest.TARGET, writer.toByteArray()));
        boolean rejectedInjection = false;
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            String type = cause.getClass().getName();
            if (type.equals("org.spongepowered.asm.mixin.injection.throwables.InjectionError")
                    || type.equals("org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException")) {
                System.out.println("Injection rejection: " + cause.getMessage());
                String expected = mutation.equals("missing") ? "(0/1) succeeded" : "2 succeeded of 1 allowed";
                rejectedInjection = cause.getMessage().contains(expected);
            }
        }
        assertTrue(rejectedInjection, "must fail for injection count, not an unrelated error: " + error);
        System.out.println("PASS: rejected " + mutation + " polygon-query target");
    }
}
