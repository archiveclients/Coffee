/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package cf.coffee.client.feature.module.impl.world;

import cf.coffee.client.CoffeeMain;
import cf.coffee.client.feature.config.BooleanSetting;
import cf.coffee.client.feature.gui.notifications.Notification;
import cf.coffee.client.feature.gui.notifications.NotificationRenderer;
import cf.coffee.client.feature.module.Module;
import cf.coffee.client.feature.module.ModuleType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Objects;

public class GodBridge extends Module {

    final float mOffset = 0.20f;
    final Direction[] allowedSides = new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };
    //    final BooleanValue courseCorrect = (BooleanValue) this.config.create("Course correct", true).description("Prevent you from falling off the track by accident");
    final BooleanSetting courseCorrect = this.config.create(new BooleanSetting.Builder(true).name("Course correct").description("Prevents you from accidentally falling off a side of the bridge").get());
    Notification isReady = null;

    public GodBridge() {
        super("GodBridge", "Automatically makes you look like a badass", ModuleType.WORLD);
    }

    boolean isReady() {
        return Objects.requireNonNull(client.player).getPitch() > 82;
    }

    @Override
    public void tick() {
        // Notification.create(5000, "GodBridge", "Look down, as you would normally while godbridging to start");
        if (!isReady()) {
            if (isReady == null) {
                isReady = Notification.create(-1, "GodBridge", true, Notification.Type.INFO, "Look down, as you would normally while godbridging to start");
            }
        } else {
            if (isReady != null) {
                isReady.duration = 0;
            }
        }
        if (!NotificationRenderer.topBarNotifications.contains(isReady)) {
            isReady = null;
        }
    }

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
        if (isReady != null) {
            isReady.duration = 0;
        }
    }

    @Override
    public String getContext() {
        return isReady() ? "Ready" : "Not ready";
    }

    @Override
    public void onWorldRender(MatrixStack matrices) {

    }

    @Override
    public void onHudRender() {

    }

    @Override
    public void onFastTick() {
        if (!isReady()) {
            return;
        }
        Objects.requireNonNull(client.player).setYaw(client.player.getMovementDirection().asRotation());
        if (client.player.getPitch() > 83) {
            client.player.setPitch(82.5f);
        }
        HitResult hr = client.crosshairTarget;
        if (Objects.requireNonNull(hr).getType() == HitResult.Type.BLOCK && hr instanceof BlockHitResult result) {
            if (Arrays.stream(allowedSides).anyMatch(direction -> direction == result.getSide())) {
                CoffeeMain.client.execute(() -> {
                    client.player.swingHand(Hand.MAIN_HAND);
                    Objects.requireNonNull(client.interactionManager).interactBlock(client.player, client.world, Hand.MAIN_HAND, result);
                });
            }
        }
        if (!courseCorrect.getValue()) {
            return;
        }
        Vec3d ppos = client.player.getPos();
        Vec3d isolated = new Vec3d(ppos.x - Math.floor(ppos.x), 0, ppos.z - Math.floor(ppos.z));
        double toCheck = 0;
        switch (client.player.getMovementDirection()) {
            case NORTH, SOUTH -> toCheck = isolated.x;
            case EAST, WEST -> toCheck = isolated.z;
        }
        client.options.sneakKey.setPressed(toCheck > 0.5 + mOffset || toCheck < 0.5 - mOffset);
    }

}
