/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.mixin;

import cf.coffee.client.feature.module.ModuleRegistry;
import cf.coffee.client.feature.module.impl.render.ESP;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModelPart.Cuboid.class)
public class ModelPartCuboidMixin {
    @Redirect(method = "renderCuboid", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;vertex(FFFFFFFFFIIFFF)V"))
    void bruh(VertexConsumer instance, float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
        instance.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
        if (ModuleRegistry.getByClass(ESP.class).recording) {
            ModuleRegistry.getByClass(ESP.class).vertexDumps.add(new double[] { x, y, z });
        }
    }

}
