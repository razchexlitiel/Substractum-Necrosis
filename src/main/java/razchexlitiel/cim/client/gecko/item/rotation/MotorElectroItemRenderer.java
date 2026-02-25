package razchexlitiel.cim.client.gecko.item.rotation;


import razchexlitiel.cim.item.rotation.MotorElectroBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class MotorElectroItemRenderer extends GeoItemRenderer<MotorElectroBlockItem> {
    public MotorElectroItemRenderer() {
        super(new MotorElectroItemModel());
    }
}