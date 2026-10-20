/*
 * SPDX-License-Identifier: LGPL-3.0-only
 * Copyright (C) 2026 mannie-exe
 * Backports Distant Horizons !126 / 51bf1f319b56e233ac8e0e8189cd7350444da0f6.
 * DH GLState: Copyright (C) 2020 James Seibel. See NOTICE.
 */
package io.github.mannieexe.dhpolygonfix.mixin;

import com.seibel.distanthorizons.lwjgl.ILWJGLService;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(targets = "com.seibel.distanthorizons.common.render.openGl.glObject.GLState", remap = false)
public abstract class GLStateMixin {
    @Redirect(
        method = "saveState()V",
        slice = @Slice(from = @At(value = "FIELD",
            target = "Lcom/seibel/distanthorizons/common/render/openGl/glObject/GLState;cullMode:I",
            opcode = 181)),
        at = @At(value = "INVOKE",
            target = "Lcom/seibel/distanthorizons/lwjgl/ILWJGLService;glGetInteger(I)I"),
        require = 1, expect = 1, allow = 1
    )
    private int dhPolygonFix$queryPolygonModes(ILWJGLService service, int pname) {
        // Fail clearly if a changed target would redirect a different query.
        if (pname != GL11.GL_POLYGON_MODE) {
            throw new IllegalStateException("DH polygon fix encountered an unexpected query: " + pname);
        }
        // Drivers may return front and back modes. The scalar helper reserves one int.
        // Keep upstream semantics: store the first mode, even if the driver writes two.
        int[] modes = new int[2];
        service.glGetIntegerv(pname, modes);
        return modes[0];
    }
}
