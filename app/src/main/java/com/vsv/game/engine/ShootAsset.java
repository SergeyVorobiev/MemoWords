package com.vsv.game.engine;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.vsv.game.engine.objects.TextureRegionProperties;
import com.vsv.memorizer.R;
import com.vsv.utils.StaticUtils;

public class ShootAsset implements Asset {

    private static ShootAsset asset;

    public final Bitmap[] textures = new Bitmap[5];

    public final TextureRegionProperties cannonBarrel = new TextureRegionProperties(512, 512, 0, 0, 1, 0, 2048, 2048);

    public final TextureRegionProperties cannonCloud = new TextureRegionProperties(512, 512, 512, 0, 1, 0, 2048, 2048);

    public final TextureRegionProperties cannonGauss = new TextureRegionProperties(512, 512, 1024, 0, 1, 0, 2048, 2048);

    public final TextureRegionProperties cannonRocket = new TextureRegionProperties(512, 512, 1536, 0, 1, 0, 2048, 2048);

    public final TextureRegionProperties cannonBasement = new TextureRegionProperties(512, 512, 0, 1024, 1, 0, 2048, 2048);

    public final TextureRegionProperties fireBallExplosion = new TextureRegionProperties(768, 128, 256, 512, 6, 0, 2048, 2048);

    public final TextureRegionProperties rocketExplosion = new TextureRegionProperties(768, 128, 256, 640, 6, 0, 2048, 2048);

    public final TextureRegionProperties whiteSmoke = new TextureRegionProperties(768, 128, 1280, 1792, 6, 0, 2048, 2048);

    public final TextureRegionProperties blackSmoke = new TextureRegionProperties(768, 128, 512, 1792, 6, 0, 2048, 2048);

    public final TextureRegionProperties textCloud = new TextureRegionProperties(512, 512, 1024, 512, 1, 0, 2048, 2048);

    public final TextureRegionProperties cannonGun = new TextureRegionProperties(512, 512, 1536, 512, 1, 0, 2048, 2048);

    public final TextureRegionProperties gaussExplosion = new TextureRegionProperties(128, 128, 0, 768, 1, 0, 2048, 2048);

    public final TextureRegionProperties laser = new TextureRegionProperties(128, 128, 128, 768, 1, 0, 2048, 2048);

    public final TextureRegionProperties galaxy = new TextureRegionProperties(128, 128, 0, 896, 1, 0, 2048, 2048);

    public final TextureRegionProperties star = new TextureRegionProperties(128, 128, 128, 896, 1, 0, 2048, 2048);

    public final TextureRegionProperties rocket = new TextureRegionProperties(512, 256, 512, 768, 1, 0, 2048, 2048);

    public final TextureRegionProperties dust = new TextureRegionProperties(2048, 128, 0, 1920, 16, 0, 2048, 2048);

    public final TextureRegionProperties bigFireBullet = new TextureRegionProperties(128, 128, 0, 1792, 1, 0, 2048, 2048);

    public final TextureRegionProperties fireBullet = new TextureRegionProperties(128, 128, 256, 512, 1, 0, 2048, 2048);

    public final TextureRegionProperties toyRow1 = new TextureRegionProperties(1536, 256, 512, 1024, 6, 0, 2048, 2048);

    public final TextureRegionProperties toyRow2 = new TextureRegionProperties(1526, 256, 512, 1280, 6, 0, 2048, 2048);

    public final TextureRegionProperties toyRow3 = new TextureRegionProperties(2048, 256, 0, 1536, 8, 0, 2048, 2048);

    public static ShootAsset build() {
        if (asset != null) {
            return asset;
        }
        asset = new ShootAsset();
        asset.textures[0] = getBitmap(R.raw.texture);
        asset.textures[1] = getBitmap(R.raw.acc);
        asset.textures[2] = getBitmap(R.raw.acc);
        asset.textures[3] = getBitmap(R.raw.acc);
        asset.textures[4] = getBitmap(R.raw.acc);
        return asset;
    }

    public static ShootAsset getAsset() {
        return asset;
    }

    public Bitmap getTexture(int textureId) {
        return textures[textureId];
    }

    public Bitmap getCopyTexture(int textureId, boolean mutable) {
        if (mutable) {
            return textures[textureId].copy(Bitmap.Config.ARGB_8888, true);
        }
        return Bitmap.createBitmap(textures[textureId]);
    }

    public static Bitmap getDrawable(int resource) {
        return ((BitmapDrawable) StaticUtils.getDrawable(resource)).getBitmap();
    }

    public static Bitmap getBitmap(int resource) {
        return StaticUtils.getBitmap(resource, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap resize(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    @Override
    public void destroy() {
        /*
        for (Bitmap texture : textures) {
            if (!texture.isRecycled()) {
                texture.recycle();
            }
        }*/
    }
}
