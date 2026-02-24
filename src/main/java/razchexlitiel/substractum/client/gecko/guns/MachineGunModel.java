package razchexlitiel.substractum.client.gecko.guns;


import net.minecraft.resources.ResourceLocation;
import razchexlitiel.substractum.item.guns.MachineGunItem;
import razchexlitiel.substractum.main.SubstractumMod;
import software.bernie.geckolib.model.GeoModel;

public class MachineGunModel extends GeoModel<MachineGunItem> {

    @Override
    public ResourceLocation getModelResource(MachineGunItem animatable) {
        return new ResourceLocation(SubstractumMod.MOD_ID, "geo/machinegun.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MachineGunItem animatable) {
        return new ResourceLocation(SubstractumMod.MOD_ID, "textures/item/machinegun.png");  // ✅ PNG, НЕ JPG!
    }

    @Override
    public ResourceLocation getAnimationResource(MachineGunItem animatable) {
        return new ResourceLocation(SubstractumMod.MOD_ID, "animations/machinegun.animation.json");
    }
}
