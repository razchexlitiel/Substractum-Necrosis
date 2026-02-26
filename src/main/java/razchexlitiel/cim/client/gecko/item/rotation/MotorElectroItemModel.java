package razchexlitiel.cim.client.gecko.item.rotation;


import net.minecraft.resources.ResourceLocation;
import razchexlitiel.cim.item.rotation.MotorElectroBlockItem;
import razchexlitiel.cim.main.CrustalIncursionMod;
import software.bernie.geckolib.model.GeoModel;

public class MotorElectroItemModel extends GeoModel<MotorElectroBlockItem> {
    @Override
    public ResourceLocation getModelResource(MotorElectroBlockItem animatable) {
        return new ResourceLocation(CrustalIncursionMod.MOD_ID, "geo/motor_electro.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MotorElectroBlockItem animatable) {
        return new ResourceLocation(CrustalIncursionMod.MOD_ID, "textures/block/motor_electro.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MotorElectroBlockItem animatable) {
        return new ResourceLocation(CrustalIncursionMod.MOD_ID, "animations/motor_electro.animation.json");
    }
}