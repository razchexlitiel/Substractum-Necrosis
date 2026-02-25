package razchexlitiel.cim.client.gecko.item.rotation;


import net.minecraft.resources.ResourceLocation;
import razchexlitiel.cim.item.rotation.ShaftIronBlockItem;
import razchexlitiel.cim.main.CrustalIncursionMod;
import software.bernie.geckolib.model.GeoModel;

public class ShaftIronItemModel extends GeoModel<ShaftIronBlockItem> {
    @Override
    public ResourceLocation getModelResource(ShaftIronBlockItem animatable) {
        return new ResourceLocation(CrustalIncursionMod.MOD_ID, "geo/shaft_iron.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ShaftIronBlockItem animatable) {
        return new ResourceLocation(CrustalIncursionMod.MOD_ID, "textures/block/shaft_iron.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ShaftIronBlockItem animatable) {
        return new ResourceLocation(CrustalIncursionMod.MOD_ID, "animations/shaft_iron.animation.json");
    }
}