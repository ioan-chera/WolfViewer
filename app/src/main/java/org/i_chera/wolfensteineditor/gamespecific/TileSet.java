package org.i_chera.wolfensteineditor.gamespecific;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.SparseArray;

import com.amulyakhare.textdrawable.TextDrawable;

import org.i_chera.wolfensteineditor.Global;
import org.i_chera.wolfensteineditor.R;
import org.i_chera.wolfensteineditor.document.Document;
import org.i_chera.wolfensteineditor.document.VSwapContainer;

/**
 * Created by ioan_chera on 12.11.2015.
 */
public class TileSet
{
    private static ClipDrawable sHorizontalDoor;
    private static ClipDrawable sVerticalDoor;
    private static final SparseArray<Drawable> sFloorCodes = new SparseArray<>(256);

    private static Drawable getHorizontalDoor(Resources resources)
    {
        if(sHorizontalDoor == null)
        {
            sHorizontalDoor = (ClipDrawable) resources.getDrawable(R.drawable.door_horizontal);
            if(sHorizontalDoor == null)
                throw new NullPointerException("Horizontal door is null!");
            sHorizontalDoor.setLevel(2500);
        }
        return sHorizontalDoor;
    }
    private static Drawable getVerticalDoor(Resources resources)
    {
        if(sVerticalDoor == null)
        {
            sVerticalDoor = (ClipDrawable) resources.getDrawable(R.drawable.door_vertical);
            if(sVerticalDoor == null)
                throw new NullPointerException("Vertical door is null!");
            sVerticalDoor.setLevel(2500);
        }
        return sVerticalDoor;
    }
    private static Drawable getFloorCode(int index)
    {
        Drawable drawable = sFloorCodes.get(index);
        if(drawable == null)
        {
            String text = index == 0x6a ? "Deaf" : index == 0x6b ? "Secret" : Integer.toHexString(index);
            drawable = TextDrawable.builder().beginConfig().textColor(Color.LTGRAY).toUpperCase()
                    .endConfig().buildRect(text, Color.TRANSPARENT);
            sFloorCodes.setValueAt(index, drawable);
        }
        return drawable;
    }

    // TODO: add SOD support
    public static Drawable imageForTile(Document document, Resources resources, int wall, int object)
    {
        Drawable wallDrawable;
        Drawable objectDrawable;
        VSwapContainer vSwap = document.getVSwap();

        if(wall >= 90 && wall <= 100 && wall % 2 == 0)
        {
            wallDrawable = getHorizontalDoor(resources);
        }
        else if(wall >= 91 && wall <= 101 && wall % 2 == 1)
        {
            wallDrawable = getVerticalDoor(resources);
        }
        else
        {
            int texture = 2 * (wall - 1);

            if(texture >= 0 && texture < vSwap.getSpriteStart())
            {
                wallDrawable = new BitmapDrawable(resources, vSwap.getWallBitmap(texture));
            }
            else
            {
                wallDrawable = getFloorCode(wall);
            }
        }

        int spriteIndex = Global.getActorSpriteMap().get(object, -1);
        if(spriteIndex == -1)
            objectDrawable = null;
        else
            objectDrawable = new BitmapDrawable(resources, vSwap.getSpriteBitmap(spriteIndex));

        return objectDrawable == null ? wallDrawable : new LayerDrawable(
                new Drawable[] { wallDrawable, objectDrawable });
    }
}
