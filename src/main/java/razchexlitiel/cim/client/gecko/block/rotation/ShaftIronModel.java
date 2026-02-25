package razchexlitiel.cim.client.gecko.block.rotation;


import net.minecraft.resources.ResourceLocation;
import razchexlitiel.cim.block.entity.rotation.ShaftIronBlockEntity;
import razchexlitiel.cim.main.CrustalIncursionMod;
import software.bernie.geckolib.model.GeoModel;

public class ShaftIronModel extends GeoModel<ShaftIronBlockEntity> {
    @Override
    public ResourceLocation getModelResource(ShaftIronBlockEntity animatable) {
        return new ResourceLocation(CrustalIncursionMod.MOD_ID, "geo/shaft_iron.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ShaftIronBlockEntity animatable) {
        return new ResourceLocation(CrustalIncursionMod.MOD_ID, "textures/block/shaft_iron.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ShaftIronBlockEntity animatable) {
        return new ResourceLocation(CrustalIncursionMod.MOD_ID, "animations/shaft_iron.animation.json");
    }
}