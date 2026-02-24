package razchexlitiel.substractum.item.weapons.ammo;


import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import razchexlitiel.substractum.client.gecko.ammo.AmmoTurretRenderer;
import razchexlitiel.substractum.item.tags.IAmmoItem;
import razchexlitiel.substractum.network.ModPacketHandler;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class AmmoTurretItem extends Item implements GeoItem, IAmmoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Характеристики
    private final float damage;
    private final float speed;
    private final boolean isPiercing;

    public AmmoTurretItem(Properties properties, float damage, float speed, boolean isPiercing) {
        super(properties);
        this.damage = damage;
        this.speed = speed;
        this.isPiercing = isPiercing;

        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }


    @Override
    public String getCaliber() { return "20mm_turret"; }

    @Override
    public float getDamage() { return this.damage; }

    @Override
    public float getSpeed() { return this.speed; }

    @Override
    public boolean isPiercing() { return this.isPiercing; }

    // ✅ ВОЗВРАЩАЕМ АНИМАЦИИ
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> PlayState.CONTINUE)
                .triggerableAnim("flip", RawAnimation.begin().thenPlay("flip")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private AmmoTurretRenderer renderer;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new AmmoTurretRenderer();
                return renderer;
            }
        });
    }
}
