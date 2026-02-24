package razchexlitiel.substractum.client.gecko.ammo;


import razchexlitiel.substractum.item.weapons.ammo.AmmoTurretItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class AmmoTurretRenderer extends GeoItemRenderer<AmmoTurretItem> {
    public AmmoTurretRenderer() {
        super(new AmmoTurretModel());
    }
}
