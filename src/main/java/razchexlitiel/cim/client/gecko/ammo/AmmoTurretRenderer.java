package razchexlitiel.cim.client.gecko.ammo;


import razchexlitiel.cim.item.weapons.ammo.AmmoTurretItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class AmmoTurretRenderer extends GeoItemRenderer<AmmoTurretItem> {
    public AmmoTurretRenderer() {
        super(new AmmoTurretModel());
    }
}
