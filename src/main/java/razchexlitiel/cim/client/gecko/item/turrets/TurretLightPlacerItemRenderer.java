package razchexlitiel.cim.client.gecko.item.turrets;



import razchexlitiel.cim.item.weapons.turrets.TurretLightPlacerBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class TurretLightPlacerItemRenderer extends GeoItemRenderer<TurretLightPlacerBlockItem> {
    public TurretLightPlacerItemRenderer() {
        super(new TurretLightPlacerItemModel()); // Используем правильную модель
    }
}
