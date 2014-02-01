package com.ichera.wolfviewer;

import android.graphics.Color;

public class Palette 
{
	private static int inflateRGB(int r, int g, int b)
	{
		return Color.rgb(255/63*r, 255/63*g, 255/63*b);
	}
	
	public static int[] getColors(byte[] indexed)
	{
		int[] ret = new int[indexed.length];
		for(int i = 0; i < ret.length; ++i)
			ret[i] = WL6[indexed[i] & 0xff];
		return ret;
	}
	
	public static final int[] WL6 = new int[]{
		inflateRGB(  0,  0,  0),inflateRGB(  0,  0, 42),inflateRGB(  0, 42,  0),inflateRGB(  0, 42, 42),inflateRGB( 42,  0,  0),
		inflateRGB( 42,  0, 42),inflateRGB( 42, 21,  0),inflateRGB( 42, 42, 42),inflateRGB( 21, 21, 21),inflateRGB( 21, 21, 63),
		inflateRGB( 21, 63, 21),inflateRGB( 21, 63, 63),inflateRGB( 63, 21, 21),inflateRGB( 63, 21, 63),inflateRGB( 63, 63, 21),
		inflateRGB( 63, 63, 63),inflateRGB( 59, 59, 59),inflateRGB( 55, 55, 55),inflateRGB( 52, 52, 52),inflateRGB( 48, 48, 48),
		inflateRGB( 45, 45, 45),inflateRGB( 42, 42, 42),inflateRGB( 38, 38, 38),inflateRGB( 35, 35, 35),inflateRGB( 31, 31, 31),
		inflateRGB( 28, 28, 28),inflateRGB( 25, 25, 25),inflateRGB( 21, 21, 21),inflateRGB( 18, 18, 18),inflateRGB( 14, 14, 14),
		inflateRGB( 11, 11, 11),inflateRGB(  8,  8,  8),inflateRGB( 63,  0,  0),inflateRGB( 59,  0,  0),inflateRGB( 56,  0,  0),
		inflateRGB( 53,  0,  0),inflateRGB( 50,  0,  0),inflateRGB( 47,  0,  0),inflateRGB( 44,  0,  0),inflateRGB( 41,  0,  0),
		inflateRGB( 38,  0,  0),inflateRGB( 34,  0,  0),inflateRGB( 31,  0,  0),inflateRGB( 28,  0,  0),inflateRGB( 25,  0,  0),
		inflateRGB( 22,  0,  0),inflateRGB( 19,  0,  0),inflateRGB( 16,  0,  0),inflateRGB( 63, 54, 54),inflateRGB( 63, 46, 46),
		inflateRGB( 63, 39, 39),inflateRGB( 63, 31, 31),inflateRGB( 63, 23, 23),inflateRGB( 63, 16, 16),inflateRGB( 63,  8,  8),
		inflateRGB( 63,  0,  0),inflateRGB( 63, 42, 23),inflateRGB( 63, 38, 16),inflateRGB( 63, 34,  8),inflateRGB( 63, 30,  0),
		inflateRGB( 57, 27,  0),inflateRGB( 51, 24,  0),inflateRGB( 45, 21,  0),inflateRGB( 39, 19,  0),inflateRGB( 63, 63, 54),
		inflateRGB( 63, 63, 46),inflateRGB( 63, 63, 39),inflateRGB( 63, 63, 31),inflateRGB( 63, 62, 23),inflateRGB( 63, 61, 16),
		inflateRGB( 63, 61,  8),inflateRGB( 63, 61,  0),inflateRGB( 57, 54,  0),inflateRGB( 51, 49,  0),inflateRGB( 45, 43,  0),
		inflateRGB( 39, 39,  0),inflateRGB( 33, 33,  0),inflateRGB( 28, 27,  0),inflateRGB( 22, 21,  0),inflateRGB( 16, 16,  0),
		inflateRGB( 52, 63, 23),inflateRGB( 49, 63, 16),inflateRGB( 45, 63,  8),inflateRGB( 40, 63,  0),inflateRGB( 36, 57,  0),
		inflateRGB( 32, 51,  0),inflateRGB( 29, 45,  0),inflateRGB( 24, 39,  0),inflateRGB( 54, 63, 54),inflateRGB( 47, 63, 46),
		inflateRGB( 39, 63, 39),inflateRGB( 32, 63, 31),inflateRGB( 24, 63, 23),inflateRGB( 16, 63, 16),inflateRGB(  8, 63,  8),
		inflateRGB(  0, 63,  0),inflateRGB(  0, 63,  0),inflateRGB(  0, 59,  0),inflateRGB(  0, 56,  0),inflateRGB(  0, 53,  0),
		inflateRGB(  1, 50,  0),inflateRGB(  1, 47,  0),inflateRGB(  1, 44,  0),inflateRGB(  1, 41,  0),inflateRGB(  1, 38,  0),
		inflateRGB(  1, 34,  0),inflateRGB(  1, 31,  0),inflateRGB(  1, 28,  0),inflateRGB(  1, 25,  0),inflateRGB(  1, 22,  0),
		inflateRGB(  1, 19,  0),inflateRGB(  1, 16,  0),inflateRGB( 54, 63, 63),inflateRGB( 46, 63, 63),inflateRGB( 39, 63, 63),
		inflateRGB( 31, 63, 62),inflateRGB( 23, 63, 63),inflateRGB( 16, 63, 63),inflateRGB(  8, 63, 63),inflateRGB(  0, 63, 63),
		inflateRGB(  0, 57, 57),inflateRGB(  0, 51, 51),inflateRGB(  0, 45, 45),inflateRGB(  0, 39, 39),inflateRGB(  0, 33, 33),
		inflateRGB(  0, 28, 28),inflateRGB(  0, 22, 22),inflateRGB(  0, 16, 16),inflateRGB( 23, 47, 63),inflateRGB( 16, 44, 63),
		inflateRGB(  8, 42, 63),inflateRGB(  0, 39, 63),inflateRGB(  0, 35, 57),inflateRGB(  0, 31, 51),inflateRGB(  0, 27, 45),
		inflateRGB(  0, 23, 39),inflateRGB( 54, 54, 63),inflateRGB( 46, 47, 63),inflateRGB( 39, 39, 63),inflateRGB( 31, 32, 63),
		inflateRGB( 23, 24, 63),inflateRGB( 16, 16, 63),inflateRGB(  8,  9, 63),inflateRGB(  0,  1, 63),inflateRGB(  0,  0, 63),
		inflateRGB(  0,  0, 59),inflateRGB(  0,  0, 56),inflateRGB(  0,  0, 53),inflateRGB(  0,  0, 50),inflateRGB(  0,  0, 47),
		inflateRGB(  0,  0, 44),inflateRGB(  0,  0, 41),inflateRGB(  0,  0, 38),inflateRGB(  0,  0, 34),inflateRGB(  0,  0, 31),
		inflateRGB(  0,  0, 28),inflateRGB(  0,  0, 25),inflateRGB(  0,  0, 22),inflateRGB(  0,  0, 19),inflateRGB(  0,  0, 16),
		inflateRGB( 10, 10, 10),inflateRGB( 63, 56, 13),inflateRGB( 63, 53,  9),inflateRGB( 63, 51,  6),inflateRGB( 63, 48,  2),
		inflateRGB( 63, 45,  0),inflateRGB( 45,  8, 63),inflateRGB( 42,  0, 63),inflateRGB( 38,  0, 57),inflateRGB( 32,  0, 51),
		inflateRGB( 29,  0, 45),inflateRGB( 24,  0, 39),inflateRGB( 20,  0, 33),inflateRGB( 17,  0, 28),inflateRGB( 13,  0, 22),
		inflateRGB( 10,  0, 16),inflateRGB( 63, 54, 63),inflateRGB( 63, 46, 63),inflateRGB( 63, 39, 63),inflateRGB( 63, 31, 63),
		inflateRGB( 63, 23, 63),inflateRGB( 63, 16, 63),inflateRGB( 63,  8, 63),inflateRGB( 63,  0, 63),inflateRGB( 56,  0, 57),
		inflateRGB( 50,  0, 51),inflateRGB( 45,  0, 45),inflateRGB( 39,  0, 39),inflateRGB( 33,  0, 33),inflateRGB( 27,  0, 28),
		inflateRGB( 22,  0, 22),inflateRGB( 16,  0, 16),inflateRGB( 63, 58, 55),inflateRGB( 63, 56, 52),inflateRGB( 63, 54, 49),
		inflateRGB( 63, 53, 47),inflateRGB( 63, 51, 44),inflateRGB( 63, 49, 41),inflateRGB( 63, 47, 39),inflateRGB( 63, 46, 36),
		inflateRGB( 63, 44, 32),inflateRGB( 63, 41, 28),inflateRGB( 63, 39, 24),inflateRGB( 60, 37, 23),inflateRGB( 58, 35, 22),
		inflateRGB( 55, 34, 21),inflateRGB( 52, 32, 20),inflateRGB( 50, 31, 19),inflateRGB( 47, 30, 18),inflateRGB( 45, 28, 17),
		inflateRGB( 42, 26, 16),inflateRGB( 40, 25, 15),inflateRGB( 39, 24, 14),inflateRGB( 36, 23, 13),inflateRGB( 34, 22, 12),
		inflateRGB( 32, 20, 11),inflateRGB( 29, 19, 10),inflateRGB( 27, 18,  9),inflateRGB( 23, 16,  8),inflateRGB( 21, 15,  7),
		inflateRGB( 18, 14,  6),inflateRGB( 16, 12,  6),inflateRGB( 14, 11,  5),inflateRGB( 10,  8,  3),inflateRGB( 24,  0, 25),
		inflateRGB(  0, 25, 25),inflateRGB(  0, 24, 24),inflateRGB(  0,  0,  7),inflateRGB(  0,  0, 11),inflateRGB( 12,  9,  4),
		inflateRGB( 18,  0, 18),inflateRGB( 20,  0, 20),inflateRGB(  0,  0, 13),inflateRGB(  7,  7,  7),inflateRGB( 19, 19, 19),
		inflateRGB( 23, 23, 23),inflateRGB( 16, 16, 16),inflateRGB( 12, 12, 12),inflateRGB( 13, 13, 13),inflateRGB( 54, 61, 61),
		inflateRGB( 46, 58, 58),inflateRGB( 39, 55, 55),inflateRGB( 29, 50, 50),inflateRGB( 18, 48, 48),inflateRGB(  8, 45, 45),
		inflateRGB(  8, 44, 44),inflateRGB(  0, 41, 41),inflateRGB(  0, 38, 38),inflateRGB(  0, 35, 35),inflateRGB(  0, 33, 33),
		inflateRGB(  0, 31, 31),inflateRGB(  0, 30, 30),inflateRGB(  0, 29, 29),inflateRGB(  0, 28, 28),inflateRGB(  0, 27, 27),
		inflateRGB( 38,  0, 34)
	};
	
	public static final int[] SOD = new int[]{
		inflateRGB(  0,  0,  0),inflateRGB(  0,  0, 42),inflateRGB(  0, 42,  0),inflateRGB(  0, 42, 42),inflateRGB( 42,  0,  0),
		inflateRGB( 42,  0, 42),inflateRGB( 42, 21,  0),inflateRGB( 42, 42, 42),inflateRGB( 21, 21, 21),inflateRGB( 21, 21, 63),
		inflateRGB( 21, 63, 21),inflateRGB( 21, 63, 63),inflateRGB( 63, 21, 21),inflateRGB( 63, 21, 63),inflateRGB( 63, 63, 21),
		inflateRGB( 63, 63, 63),inflateRGB( 59, 59, 59),inflateRGB( 55, 55, 55),inflateRGB( 52, 52, 52),inflateRGB( 48, 48, 48),
		inflateRGB( 45, 45, 45),inflateRGB( 42, 42, 42),inflateRGB( 38, 38, 38),inflateRGB( 35, 35, 35),inflateRGB( 31, 31, 31),
		inflateRGB( 28, 28, 28),inflateRGB( 25, 25, 25),inflateRGB( 21, 21, 21),inflateRGB( 18, 18, 18),inflateRGB( 14, 14, 14),
		inflateRGB( 11, 11, 11),inflateRGB(  8,  8,  8),inflateRGB( 63,  0,  0),inflateRGB( 59,  0,  0),inflateRGB( 56,  0,  0),
		inflateRGB( 53,  0,  0),inflateRGB( 50,  0,  0),inflateRGB( 47,  0,  0),inflateRGB( 44,  0,  0),inflateRGB( 41,  0,  0),
		inflateRGB( 38,  0,  0),inflateRGB( 34,  0,  0),inflateRGB( 31,  0,  0),inflateRGB( 28,  0,  0),inflateRGB( 25,  0,  0),
		inflateRGB( 22,  0,  0),inflateRGB( 19,  0,  0),inflateRGB( 16,  0,  0),inflateRGB( 63, 54, 54),inflateRGB( 63, 46, 46),
		inflateRGB( 63, 39, 39),inflateRGB( 63, 31, 31),inflateRGB( 63, 23, 23),inflateRGB( 63, 16, 16),inflateRGB( 63,  8,  8),
		inflateRGB( 63,  0,  0),inflateRGB( 63, 42, 23),inflateRGB( 63, 38, 16),inflateRGB( 63, 34,  8),inflateRGB( 63, 30,  0),
		inflateRGB( 57, 27,  0),inflateRGB( 51, 24,  0),inflateRGB( 45, 21,  0),inflateRGB( 39, 19,  0),inflateRGB( 63, 63, 54),
		inflateRGB( 63, 63, 46),inflateRGB( 63, 63, 39),inflateRGB( 63, 63, 31),inflateRGB( 63, 62, 23),inflateRGB( 63, 61, 16),
		inflateRGB( 63, 61,  8),inflateRGB( 63, 61,  0),inflateRGB( 57, 54,  0),inflateRGB( 51, 49,  0),inflateRGB( 45, 43,  0),
		inflateRGB( 39, 39,  0),inflateRGB( 33, 33,  0),inflateRGB( 28, 27,  0),inflateRGB( 22, 21,  0),inflateRGB( 16, 16,  0),
		inflateRGB( 52, 63, 23),inflateRGB( 49, 63, 16),inflateRGB( 45, 63,  8),inflateRGB( 40, 63,  0),inflateRGB( 36, 57,  0),
		inflateRGB( 32, 51,  0),inflateRGB( 29, 45,  0),inflateRGB( 24, 39,  0),inflateRGB( 54, 63, 54),inflateRGB( 47, 63, 46),
		inflateRGB( 39, 63, 39),inflateRGB( 32, 63, 31),inflateRGB( 24, 63, 23),inflateRGB( 16, 63, 16),inflateRGB(  8, 63,  8),
		inflateRGB(  0, 63,  0),inflateRGB(  0, 63,  0),inflateRGB(  0, 59,  0),inflateRGB(  0, 56,  0),inflateRGB(  0, 53,  0),
		inflateRGB(  1, 50,  0),inflateRGB(  1, 47,  0),inflateRGB(  1, 44,  0),inflateRGB(  1, 41,  0),inflateRGB(  1, 38,  0),
		inflateRGB(  1, 34,  0),inflateRGB(  1, 31,  0),inflateRGB(  1, 28,  0),inflateRGB(  1, 25,  0),inflateRGB(  1, 22,  0),
		inflateRGB(  1, 19,  0),inflateRGB(  1, 16,  0),inflateRGB( 54, 63, 63),inflateRGB( 46, 63, 63),inflateRGB( 39, 63, 63),
		inflateRGB( 31, 63, 62),inflateRGB( 23, 63, 63),inflateRGB( 16, 63, 63),inflateRGB(  8, 63, 63),inflateRGB(  0, 63, 63),
		inflateRGB(  0, 57, 57),inflateRGB(  0, 51, 51),inflateRGB(  0, 45, 45),inflateRGB(  0, 39, 39),inflateRGB(  0, 33, 33),
		inflateRGB(  0, 28, 28),inflateRGB(  0, 22, 22),inflateRGB(  0, 16, 16),inflateRGB( 23, 47, 63),inflateRGB( 16, 44, 63),
		inflateRGB(  8, 42, 63),inflateRGB(  0, 39, 63),inflateRGB(  0, 35, 57),inflateRGB(  0, 31, 51),inflateRGB(  0, 27, 45),
		inflateRGB(  0, 23, 39),inflateRGB( 54, 54, 63),inflateRGB( 46, 47, 63),inflateRGB( 39, 39, 63),inflateRGB( 31, 32, 63),
		inflateRGB( 23, 24, 63),inflateRGB( 16, 16, 63),inflateRGB(  8,  9, 63),inflateRGB(  0,  1, 63),inflateRGB(  0,  0, 63),
		inflateRGB(  0,  0, 59),inflateRGB(  0,  0, 56),inflateRGB(  0,  0, 53),inflateRGB(  0,  0, 50),inflateRGB(  0,  0, 47),
		inflateRGB(  0,  0, 44),inflateRGB(  0,  0, 41),inflateRGB(  0,  0, 38),inflateRGB(  0,  0, 34),inflateRGB(  0,  0, 31),
		inflateRGB(  0,  0, 28),inflateRGB(  0,  0, 25),inflateRGB(  0,  0, 22),inflateRGB(  0,  0, 19),inflateRGB(  0,  0, 16),
		inflateRGB( 10, 10, 10),inflateRGB( 63, 56, 13),inflateRGB( 63, 53,  9),inflateRGB( 63, 51,  6),inflateRGB( 63, 48,  2),
		inflateRGB( 63, 45,  0),inflateRGB(  0, 14,  0),inflateRGB(  0, 10,  0),inflateRGB( 38,  0, 57),inflateRGB( 32,  0, 51),
		inflateRGB( 29,  0, 45),inflateRGB( 24,  0, 39),inflateRGB( 20,  0, 33),inflateRGB( 17,  0, 28),inflateRGB( 13,  0, 22),
		inflateRGB( 10,  0, 16),inflateRGB( 63, 54, 63),inflateRGB( 63, 46, 63),inflateRGB( 63, 39, 63),inflateRGB( 63, 31, 63),
		inflateRGB( 63, 23, 63),inflateRGB( 63, 16, 63),inflateRGB( 63,  8, 63),inflateRGB( 63,  0, 63),inflateRGB( 56,  0, 57),
		inflateRGB( 50,  0, 51),inflateRGB( 45,  0, 45),inflateRGB( 39,  0, 39),inflateRGB( 33,  0, 33),inflateRGB( 27,  0, 28),
		inflateRGB( 22,  0, 22),inflateRGB( 16,  0, 16),inflateRGB( 63, 58, 55),inflateRGB( 63, 56, 52),inflateRGB( 63, 54, 49),
		inflateRGB( 63, 53, 47),inflateRGB( 63, 51, 44),inflateRGB( 63, 49, 41),inflateRGB( 63, 47, 39),inflateRGB( 63, 46, 36),
		inflateRGB( 63, 44, 32),inflateRGB( 63, 41, 28),inflateRGB( 63, 39, 24),inflateRGB( 60, 37, 23),inflateRGB( 58, 35, 22),
		inflateRGB( 55, 34, 21),inflateRGB( 52, 32, 20),inflateRGB( 50, 31, 19),inflateRGB( 47, 30, 18),inflateRGB( 45, 28, 17),
		inflateRGB( 42, 26, 16),inflateRGB( 40, 25, 15),inflateRGB( 39, 24, 14),inflateRGB( 36, 23, 13),inflateRGB( 34, 22, 12),
		inflateRGB( 32, 20, 11),inflateRGB( 29, 19, 10),inflateRGB( 27, 18,  9),inflateRGB( 23, 16,  8),inflateRGB( 21, 15,  7),
		inflateRGB( 18, 14,  6),inflateRGB( 16, 12,  6),inflateRGB( 14, 11,  5),inflateRGB( 10,  8,  3),inflateRGB( 24,  0, 25),
		inflateRGB(  0, 25, 25),inflateRGB(  0, 24, 24),inflateRGB(  0,  0,  7),inflateRGB(  0,  0, 11),inflateRGB( 12,  9,  4),
		inflateRGB( 18,  0, 18),inflateRGB( 20,  0, 20),inflateRGB(  0,  0, 13),inflateRGB(  7,  7,  7),inflateRGB( 19, 19, 19),
		inflateRGB( 23, 23, 23),inflateRGB( 16, 16, 16),inflateRGB( 12, 12, 12),inflateRGB( 13, 13, 13),inflateRGB( 54, 61, 61),
		inflateRGB( 46, 58, 58),inflateRGB( 39, 55, 55),inflateRGB( 29, 50, 50),inflateRGB( 18, 48, 48),inflateRGB(  8, 45, 45),
		inflateRGB(  8, 44, 44),inflateRGB(  0, 41, 41),inflateRGB(  0, 38, 38),inflateRGB(  0, 35, 35),inflateRGB(  0, 33, 33),
		inflateRGB(  0, 31, 31),inflateRGB(  0, 30, 30),inflateRGB(  0, 29, 29),inflateRGB(  0, 28, 28),inflateRGB(  0, 27, 27),
		inflateRGB( 38,  0, 34)
	};
}
