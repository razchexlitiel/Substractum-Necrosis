package com.cim.client.gecko.block.energy;

import com.cim.block.entity.energy.ConnectorBlockEntity;
import com.cim.main.CrustalIncursionMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ConnectorModel extends GeoModel<ConnectorBlockEntity> {

    @Override
    public ResourceLocation getModelResource(ConnectorBlockEntity animatable) {
        return new ResourceLocation(CrustalIncursionMod.MOD_ID, "geo/connector.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ConnectorBlockEntity animatable) {
        return new ResourceLocation(CrustalIncursionMod.MOD_ID, "textures/block/connector.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ConnectorBlockEntity animatable) {
        return new ResourceLocation(CrustalIncursionMod.MOD_ID, "animations/empty.animation.json");
    }
}
