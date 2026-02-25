package razchexlitiel.cim.client.gecko.item.rotation;


import razchexlitiel.cim.item.rotation.ShaftIronBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ShaftIronItemRenderer extends GeoItemRenderer<ShaftIronBlockItem> {
    public ShaftIronItemRenderer() {
        super(new ShaftIronItemModel());
    }
}