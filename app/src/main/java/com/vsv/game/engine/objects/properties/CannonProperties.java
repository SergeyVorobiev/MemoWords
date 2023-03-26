package com.vsv.game.engine.objects.properties;

import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ObjectManager;
import com.vsv.game.engine.ShootAsset;
import com.vsv.game.engine.objects.BigFireBullet;
import com.vsv.game.engine.objects.Cannon;
import com.vsv.game.engine.objects.FireBullet;
import com.vsv.game.engine.objects.Laser;
import com.vsv.game.engine.objects.ProjectileTurret;
import com.vsv.game.engine.objects.Rocket;
import com.vsv.game.engine.objects.SmokeParams;
import com.vsv.game.engine.screens.ScreenParameters;
import com.vsv.game.engine.objects.StationaryBigProjectileTurret;
import com.vsv.game.engine.objects.StationaryDoubleProjectileTurret;

public class CannonProperties {

    public static final float width = 192;

    public static final float height = 192;

    public static void spawn(int index, ObjectManager<GameObject> objectManager) {
        Cannon cannon = null;
        float scaledWidth = ScreenParameters.screenScaleFactor * width;
        float scaledHeight = ScreenParameters.screenScaleFactor * height;
        float part = (ScreenParameters.screenWidth - ScreenParameters.shiftWidth * 2) / 4.0f;
        float shift = part / 2.0f + ScreenParameters.shiftWidth;
        float y = ScreenParameters.screenHeight - scaledHeight / 2 - ScreenParameters.shiftHeight;
        if (index == 0) {
            cannon = (Cannon) new Cannon(scaledWidth, scaledHeight, ShootAsset.getAsset().cannonGauss,
                    new ProjectileTurret(0, 0, 1, 0.1f, 2500, null, Laser.class))
                    .isLaser().install(shift, y, objectManager).setMaxRotationSpeed(90).setRotateAngle(90);
        }
        if (index == 1) {
            // SmokeParams smoke = new SmokeParams(ShootAsset.getAsset().whiteSmoke, 72, 72, 0.1f, -20, 20, -20, -20);
            cannon = (Cannon) new Cannon(scaledWidth, scaledHeight, ShootAsset.getAsset().cannonRocket,
                    new StationaryDoubleProjectileTurret(400 * ScreenParameters.screenScaleFactor, 0.06f, 4,
                            0.1f, 250, null, 36 * ScreenParameters.screenScaleFactor, Rocket.class)) //125
                    .install(shift + part, y, objectManager).setMaxRotationSpeed(0).setRotateAngle(90);
        }
        if (index == 2) {
            SmokeParams smoke = new SmokeParams(ShootAsset.getAsset().whiteSmoke, 32, 32, 0.2f, -20, 20, -50, -50);
            cannon = (Cannon) new Cannon(scaledWidth, scaledHeight, ShootAsset.getAsset().cannonGun,
                    new StationaryDoubleProjectileTurret(5000 * ScreenParameters.screenScaleFactor, 0.01f, 16,
                            0.1f, 70, smoke, 33 * ScreenParameters.screenScaleFactor, FireBullet.class))
                    .install(shift + part * 2, y, objectManager).setMaxRotationSpeed(90).setRotateAngle(90); // 65
        }
        if (index == 3) {
            SmokeParams smoke = new SmokeParams(ShootAsset.getAsset().whiteSmoke, 96, 96, 0.2f, -20, 20, -50, -50);
            cannon = (Cannon) new Cannon(scaledWidth, scaledHeight, ShootAsset.getAsset().cannonBasement,
                    new StationaryBigProjectileTurret(5000 * ScreenParameters.screenScaleFactor, 0.15f, 3,
                            0.1f, 350, smoke, BigFireBullet.class))
                    .install(shift + part * 3, y, objectManager).setMaxRotationSpeed(90).setRotateAngle(90); // 250
        }
        if (cannon == null) {
            throw new RuntimeException("Wrong index of cannon: " + index);
        }
    }
}
