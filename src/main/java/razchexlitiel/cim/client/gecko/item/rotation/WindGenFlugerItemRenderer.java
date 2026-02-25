package razchexlitiel.cim.client.gecko.item.rotation;


import razchexlitiel.cim.item.rotation.WindGenFlugerBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class WindGenFlugerItemRenderer extends GeoItemRenderer<WindGenFlugerBlockItem> {
    public WindGenFlugerItemRenderer() {
        super(new WindGenFlugerItemModel());
    }
}