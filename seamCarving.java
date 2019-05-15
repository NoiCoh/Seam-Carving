package seamCarving;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


public class seamCarving {

	public static void main(String[] args) {
		String imagePath = args[0];
		int outWidth = Integer.parseInt(args[1]);
		int outHeight = Integer.parseInt(args[2]);
		int energyType = Integer.parseInt(args[3]);
		String outputPath = args[4];

		File fImage = new File(imagePath);

		try {
			BufferedImage image = ImageIO.read(fImage);
			int width = image.getWidth();
			int height = image.getHeight();
			int widthDiff = outWidth - width;
			int heightDiff = outHeight - height;
			if (widthDiff > 0) {
				int largeScale = 2 * widthDiff / width;
				if (largeScale > 1) {
					while (widthDiff / image.getWidth() > 0.2) {
						image = addKSeams(image, energyType, (int) Math.floor(image.getWidth() * 0.18));
						widthDiff -= (int) Math.floor(image.getWidth() * 0.18);
					}

				}
				image = addKSeams(image, energyType, widthDiff);
			}
			if (heightDiff > 0) {
				int largeScale = 2 * heightDiff / height;
				image = transposeImage(image);
				if (largeScale > 1) {
					while (heightDiff / image.getHeight() > 0.2) {
						image = addKSeams(image, energyType, (int) Math.floor(image.getHeight() * 0.18));
						heightDiff -= (int) Math.floor(image.getHeight() * 0.18);
					}
				}
				image = addKSeams(image, energyType, heightDiff);
				image = transposeImage(image);

			}

			if (widthDiff < 0) {
				widthDiff = Math.abs(widthDiff);
				for (int i = 0; i < widthDiff; i++) {
					image = removeSeam(image, energyType);
				}

			}
			if (heightDiff < 0) {
				image = transposeImage(image);
				heightDiff = Math.abs(heightDiff);
				for (int i = 0; i < heightDiff; i++) {
					image = removeSeam(image, energyType);
				}
				image = transposeImage(image);

			}

			File outputFile = new File(outputPath);
			ImageIO.write(image, "jpg", outputFile);


		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static BufferedImage addKSeams(BufferedImage image, int energyType, int k) {
		int height = image.getHeight();
		int width = image.getWidth();
		BufferedImage outImage = new BufferedImage(width + k, height,  image.getType());
		int[][] seam = new int[k][image.getHeight()];
		int[][][] energyMapOriginalIndex = energyMap(image, energyType);
		int[] tempSeam = new int[image.getHeight()];
		for (int s = 0; s < k; s++) {
			if (energyType < 2) {
				tempSeam = findSeam(BackwardEnergy(energyMapOriginalIndex));
			} else {
				tempSeam = findSeam(ForwardEnergy(energyMapOriginalIndex, image));
			}
			for (int p = 0; p < image.getHeight(); p++) {
				seam[s][p] = energyMapOriginalIndex[image.getHeight() - 1 - p][tempSeam[p]][1];
			}
			energyMapOriginalIndex = removeSeamFromEM(energyMapOriginalIndex, tempSeam);
		}

		

		int[][] duplicateMark = new int[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < k; j++) {
				duplicateMark[i][seam[j][height - 1 - i]] = 1;
			}
		}
		int[][] blend = new int[height][width + k];
		for (int i = 0; i < height; i++) {
			int y = 0;
			for (int j = 0; j < width; j++) {
				if (duplicateMark[i][j] == 1) {
					outImage.setRGB(y, i, image.getRGB(j, i));
					outImage.setRGB(y + 1, i, image.getRGB(j, i));
					blend[i][y + 1] = 1;
					y += 2;
				} else {
					outImage.setRGB(y, i, image.getRGB(j, i));
					y++;
				}
			}
		}
		outImage = blendPix(outImage, blend);
		return outImage;
	}

	public static BufferedImage blendPix(BufferedImage image, int[][] blend) {
		for (int i = 0; i < image.getHeight(); i++) {
			for (int j = 1; j < image.getWidth() - 1; j++) {
				if (blend[i][j] == 1) {

					int rgb1 = image.getRGB(j - 1, i);
					int a1 = (rgb1 >> 24) & 0xff;
					int r1 = (rgb1 >> 16) & 0xFF;
					int g1 = (rgb1 >> 8) & 0xFF;
					int b1 = (rgb1 & 0xFF);
					Color temp1 = new Color(r1, g1, b1, a1);
					int rgb2 = image.getRGB(j + 1, i);
					int a2 = (rgb2 >> 24) & 0xff;
					int r2 = (rgb2 >> 16) & 0xFF;
					int g2 = (rgb2 >> 8) & 0xFF;
					int b2 = (rgb2 & 0xFF);
					Color temp2 = new Color(r2, g2, b2, a2);
					temp1 = blend(temp1, temp2);
					image.setRGB(j, i, temp1.getRGB());

				}
			}
		}
		return image;
	}

	public static Color blend(Color color1, Color color2) {
		double sum = color1.getAlpha() + color2.getAlpha();
		double weight1 = color1.getAlpha() / sum;
		double weight2 = color1.getAlpha() / sum;

		double r = weight1 * color1.getRed() + weight2 * color2.getRed();
		double g = weight1 * color1.getGreen() + weight2 * color2.getGreen();
		double b = weight1 * color1.getBlue() + weight2 * color2.getBlue();
		double a = Math.max(color1.getAlpha(), color2.getAlpha());

		return new Color((int) r, (int) g, (int) b, (int) a);
	}

	public static int[][][] removeSeamFromEM(int[][][] energyMapOriginalIndex, int[] seam) {
		int height = energyMapOriginalIndex.length;
		int width = energyMapOriginalIndex[0].length;
		int[][][] output = new int[height][width - 1][2];
		for (int i = 0; i < height; i++) {
			int insert = 0;
			for (int j = 0; j < width; j++) {
				if (seam[seam.length - 1 - i] == j) {
					continue;
				}
				output[i][insert] = energyMapOriginalIndex[i][j];
				insert++;

			}
		}

		return output;
	}

	public static int[][][] MakeRBGArray(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[][][] RGBArray = new int[height][width][3];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int pixel = image.getRGB(j, i);
				RGBArray[i][j][0] = (pixel >> 16) & 0xff; // red
				RGBArray[i][j][1] = (pixel >> 8) & 0xff; // green
				RGBArray[i][j][2] = (pixel) & 0xff; // blue

			}
		}
		return RGBArray;
	}

	/*
	 * the result of the function is Matrix with the energy of every pixel if
	 * energyType=1 : call entropyMap function
	 */
	public static int[][][] energyMap(BufferedImage image, int energyType) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[][][] RGBArray = MakeRBGArray(image);
		int[][][] energyMap = new int[height][width][2];

		for (int x = 0; x < height; x++) {
			for (int y = 0; y < width; y++) {
				int xi = x - 1;
				int yi = y - 1;
				int xe = x + 1;
				int ye = y + 1;
				int num = 8;
				if (x == 0) {
					xi = x;
					num -= 3;
				}
				if (y == 0) {
					yi = y;
					num -= 3;
				}
				if (x == height - 1) {
					xe -= 1;
					num -= 3;
				}
				if (y == width - 1) {
					ye -= 1;
					num -= 3;
				}
				if (num == 2) {
					num++;
				}
				int energy_xy = 0;
				for (int k = xi; k < xe + 1; k++) {
					for (int p = yi; p < ye + 1; p++) {

						int val = Math.abs(RGBArray[k][p][0] - RGBArray[x][y][0])
								+ Math.abs(RGBArray[k][p][1] - RGBArray[x][y][1])
								+ Math.abs(RGBArray[k][p][2] - RGBArray[x][y][2]);
						val = val / 3;
						energy_xy += val;
					}
				}
				energyMap[x][y][0] = energy_xy / num;
				energyMap[x][y][1] = y;

			}
		}
		if (energyType != 0) {
			energyMap = entropyMap(energyMap, image);
		}

		return energyMap;
	}

	public static int[][][] ForwardEnergy(int[][][] map, BufferedImage image) {
		int width = map[0].length;
		int height = map.length;
		int[][][] RGBArray = MakeRBGArray(image);
		for (int i = 1; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (j == 0) {
					int CR = (Math.abs(RGBArray[i - 1][j][0] - RGBArray[i][j + 1][0])
							+ Math.abs(RGBArray[i - 1][j][1] - RGBArray[i][j + 1][1])
							+ Math.abs(RGBArray[i - 1][j][2] - RGBArray[i][j + 1][2])) / 3;
					map[i][j][0] = map[i][j][0] + Math.min(map[i - 1][j][0], map[i - 1][j + 1][0] + CR);
				} else if (j == width - 1) {
					int CL = (Math.abs(RGBArray[i - 1][j][0] - RGBArray[i][j - 1][0])
							+ Math.abs(RGBArray[i - 1][j][1] - RGBArray[i][j - 1][1])
							+ Math.abs(RGBArray[i - 1][j][2] - RGBArray[i][j - 1][2])) / 3;
					map[i][j][0] = map[i][j][0] + Math.min(map[i - 1][j - 1][0] + CL, map[i - 1][j][0]);
				} else {
					int CU = (Math.abs(RGBArray[i][j + 1][0] - RGBArray[i][j - 1][0])
							+ Math.abs(RGBArray[i][j + 1][1] - RGBArray[i][j - 1][1])
							+ Math.abs(RGBArray[i][j + 1][2] - RGBArray[i][j - 1][2])) / 3;
					int CL = CU + (Math.abs(RGBArray[i - 1][j][0] - RGBArray[i][j - 1][0])
							+ Math.abs(RGBArray[i - 1][j][1] - RGBArray[i][j - 1][1])
							+ Math.abs(RGBArray[i - 1][j][2] - RGBArray[i][j - 1][2])) / 3;
					int CR = CU + (Math.abs(RGBArray[i - 1][j][0] - RGBArray[i][j + 1][0])
							+ Math.abs(RGBArray[i - 1][j][1] - RGBArray[i][j + 1][1])
							+ Math.abs(RGBArray[i - 1][j][2] - RGBArray[i][j + 1][2])) / 3;
					map[i][j][0] = map[i][j][0] + Math.min(map[i - 1][j - 1][0] + CL,
							Math.min(map[i - 1][j][0] + CU, map[i - 1][j + 1][0] + CR));
				}
			}
		}
		return map;
	}

	public static int[][][] entropyMap(int[][][] energyMap, BufferedImage image) {
		int[][] grayedImage = new int[image.getHeight()][image.getWidth()];
		for (int x = 0; x < image.getHeight(); ++x) {
			for (int y = 0; y < image.getWidth(); ++y) {
				int rgb = image.getRGB(y, x);
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = (rgb & 0xFF);
				int avg = (r + g + b) / 3;

				grayedImage[x][y] = avg;

			}
		}
		int[][] Pmn = new int[image.getHeight()][image.getWidth()];
		for (int m = 0; m < image.getHeight(); m++) {
			for (int n = 0; n < image.getWidth(); n++) {
				for (int x = n - 4; x < n + 5; x++) {
					for (int y = m - 4; y < m + 5; y++) {
						if (x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight()) {
							continue;
						}

						Pmn[m][n] += grayedImage[y][x];
					}
				}
				if (grayedImage[m][n] == 0) {
					Pmn[m][n] = 0;
				} else {
					Pmn[m][n] = grayedImage[m][n] / (Pmn[m][n]);
				}
			}
		}
		for (int x = 0; x < image.getHeight(); x++) {
			for (int y = 0; y < image.getWidth(); y++) {
				int temp = 0;
				for (int m = x - 4; m < x + 5; m++) {
					for (int n = y - 4; n < y + 5; n++) {
						if (m < 0 || m >= image.getHeight() || n < 0 || n >= image.getWidth() || Pmn[m][n] == 0) {
							continue;
						}

						temp += Pmn[m][n] * Math.log(Pmn[m][n]);
					}
				}
				energyMap[x][y][0] += -1 * temp;
			}
		}
		return energyMap;
	}

	static int[][][] BackwardEnergy(int[][][] map) {
		int width = map[0].length;
		int height = map.length;
		for (int i = 1; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (j == 0) {
					map[i][j][0] = map[i][j][0] + Math.min(map[i - 1][j][0], map[i - 1][j + 1][0]);
				} else if (j == width - 1) {
					map[i][j][0] = map[i][j][0] + Math.min(map[i - 1][j - 1][0], map[i - 1][j][0]);
				} else {
					map[i][j][0] = map[i][j][0]
							+ Math.min(map[i - 1][j - 1][0], Math.min(map[i - 1][j][0], map[i - 1][j + 1][0]));
				}
			}
		}
		return map;
	}

	static int[] findSeam(int[][][] energyMap) {
		int[] indexList = new int[energyMap.length];
		int[] originalIndex = new int[energyMap.length];
		int minRaw = energyMap[energyMap.length - 1][0][0];
		int index = 0;
		for (int i = 1; i < energyMap[0].length; i++) {
			if (minRaw > energyMap[energyMap.length - 1][i][0]) {
				minRaw = energyMap[energyMap.length - 1][i][0];
				index = i;
			}
		}
		originalIndex[energyMap.length - 1] = energyMap[energyMap.length - 1][index][1];
		indexList[energyMap.length - 1] = index;
		for (int j = 1; j < energyMap.length; j++) {
			for (int k = index - 1; k < index + 2; k++) {
				if (k == index || k == -1 || k == energyMap[0].length) {
					continue;
				} else {
					if (energyMap[energyMap.length - 1 - j][k][0] < energyMap[energyMap.length - 1 - j][index][0]) {
						index = k;
					}
				}
			}
			originalIndex[energyMap.length - 1 - j] = energyMap[energyMap.length - 1 - j][index][1];
			indexList[energyMap.length - 1 - j] = index;
		}

		return indexList;
	}

	static BufferedImage removeSeam(BufferedImage image, int energyType) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[] seam = new int[height];
		if (energyType < 2) {
			seam = findSeam(BackwardEnergy(energyMap(image, energyType)));
		} else {
			seam = findSeam(ForwardEnergy(energyMap(image, energyType), image));
		}
		BufferedImage outImage = new BufferedImage(width - 1, height,  image.getType());
		for (int i = 0; i < height; i++) {
			int insert = 0;
			for (int j = 0; j < width; j++) {
				if (seam[i] == j) {
					continue;
				}
				outImage.setRGB(insert, i, image.getRGB(j, i));
				insert++;
			}
		}
		return outImage;
	}

	static BufferedImage transposeImage(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage transposeImage = new BufferedImage(height, width,  image.getType());
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {

				transposeImage.setRGB(i, j, image.getRGB(j, i));
			}
		}
		return transposeImage;
	}


}