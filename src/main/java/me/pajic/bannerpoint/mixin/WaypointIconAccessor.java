package me.pajic.bannerpoint.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(Waypoint.Icon.class)
public interface WaypointIconAccessor {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Invoker("<init>")
    static Waypoint.Icon bannerpoint$callInit(final ResourceKey<WaypointStyleAsset> style, final Optional<Integer> color) {
        throw new AssertionError("Untransformed @Accessor");
    }
}
