package io.github.mannieexe.dhpolygonfix.test;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.MixinService;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/** Separate JVM so Mixin starts in the physical SERVER environment. */
public final class ServerProbe {
    public static void main(String[] args) throws Exception {
        MixinBootstrap.init();
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.SERVER);
        Mixins.addConfiguration("dh_polygon_fix.mixins.json");
        TestMixinService service = (TestMixinService) MixinService.getService();
        byte[] original;
        try (var in = service.getResourceAsStream(PatchTest.TARGET.replace('.', '/') + ".class")) {
            original = in.readAllBytes();
        }
        byte[] result = service.transformer().transformClassBytes(PatchTest.TARGET, PatchTest.TARGET, original);
        assertArrayEquals(original, result, "client mixin must not run on a dedicated server");
        System.out.println("PASS: server environment leaves DH byte-identical");
    }
}
