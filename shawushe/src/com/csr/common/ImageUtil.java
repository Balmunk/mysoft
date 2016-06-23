package com.csr.common;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

public class ImageUtil {

	
	public static final boolean SRC_NOT_NULL=true;
	public static final boolean ILLEGAL_INPUT=true;
	public static final boolean DEST_DISTORT=true;
	public static final boolean DEST_NORMAL=true;
	public static final boolean DEST_PRINT_OK=true;
	/**
	 * 测试入口
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		
	/*	File destFile = new File("D:/pic/b_bak.jpg");
		File srcFile = new File("D:/pic/b.jpg");
		imageCheck(srcFile, destFile);*/
		//图片水印
		ImageHelper.waterMark("D:/pic/b_bak.jpg", "D:/pic/logo.jpg", 10, 10, 0.3f);
		//文字水印
		ImageHelper.textMark("D:/pic/b.jpg", "中兴软创", new Font("自定义", Font.LAYOUT_RIGHT_TO_LEFT, 50), Color.black, 10,100, 0.3f);

		/*
		 * //测试缩略图 File srcFile = new File("D:/pic/b.jpg"); File destFile = new
		 * File("D:/pic/b_bak.jpg"); BufferedImage source =
		 * ImageHelper.readPNGImage(srcFile);// 读取文件 int width =4128; int height
		 * = 2322; // 第一步，缩小尺寸 // 将图片缩小到8x8的尺寸，总共64个像素 BufferedImage thumb =
		 * ImageHelper.thumb(source, width, height, false);
		 * 
		 * ByteArrayOutputStream bs =new ByteArrayOutputStream();
		 * 
		 * ImageOutputStream imOut =ImageIO.createImageOutputStream(bs);
		 * 
		 * ImageIO.write(thumb,"jpg",imOut);
		 * //scaledImage1为BufferedImage，jpg为图像的类型
		 * 
		 * InputStream is =new ByteArrayInputStream(bs.toByteArray()); int bytes
		 * = is.available(); DataOutputStream dos = new DataOutputStream(new
		 * FileOutputStream( destFile)); byte[] b = new byte[bytes]; int n =
		 * is.read(b, 0, bytes); is.close(); dos.write(b, 0, n);
		 * 
		 * dos.close();
		 */
	}
   
	public static boolean imageCheck(File srcFile,File destFile){
		
		
		if (!srcFile.isFile())
			System.out.println("文件不存在！");
		else {

			System.out.println("生成几重指纹？(请输入阿拉伯数字)");
			Scanner in = new Scanner(System.in);
			String str = in.next();
			if (!str.trim().matches("^[0-9]*$")
					|| str.trim().startsWith("0")) {
				System.out.println("输入了不合规定的字符！");
				return false;
			}
			int n = Integer.valueOf(str);
			if (destFile.isFile())
				if (isTamper(destFile, n))
					{System.out.println("是真实图片，没有篡改！");
			            return true;
					}
				else
					{System.out.println("是非法图片，请注意信息安全！");
                        return false;
					}
			else {

				System.out.println("开始添加指纹...");
				String fingerInfo = produceFingerPrint(srcFile, n);
				int picbytes = insertFingerInfo(srcFile, destFile,
						fingerInfo);

				System.out.println(n + "重指纹生成完成！");
				return true;
			}

			// int n = Integer.parseInt(in.nextLine());

		}
		return false;
		
		
	}
	
	
	public static boolean isTamper(File file, int n) {
		String fingerInfo = produceFingerPrint(file, n);
		String hiddenFingerInfo = readFingerInfo(file, n);
		return fingerInfo.equals(hiddenFingerInfo);
	}

	/**
	 * 生成图片指纹
	 * 
	 * @param filename
	 *            源文件
	 * 
	 * @return 图片指纹
	 * 
	 * @param n
	 *            指纹重数
	 */
	public static String produceFingerPrint(File file, int n) {
		BufferedImage source = ImageHelper.readPNGImage(file);// 读取文件
		// int realhei = source.getHeight();
		// int realwid =source.getWidth();
		StringBuffer hashCode = new StringBuffer();
		int width = 8;
		int height = 8;
		// 加n次指纹
		for (int o = 1; o <= n; o++) {
			// 第一步，缩小尺寸
			// 将图片缩小到8x8的尺寸，总共64个像素
			BufferedImage thumb = ImageHelper.thumb(source, width, height,
					false, n);

			// 第二步，色彩
			// 将缩小后的图片，转为64级灰度
			int[] pixels = new int[width * height];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					pixels[i * height + j] = ImageHelper.rgbToGray(thumb
							.getRGB(i, j));
				}
			}
			// 第三步，计算平均值
			int avgPixel = ImageHelper.average(pixels);
			// 第四步，比较像素的灰度
			int[] comps = new int[width * height];
			for (int i = 0; i < comps.length; i++) {
				if (pixels[i] >= avgPixel) {
					comps[i] = 1;
				} else {
					comps[i] = 0;
				}
			}
			// 第五步，计算哈希值

			for (int i = 0; i < comps.length; i += 4) {
				int result = comps[i] * (int) Math.pow(2, 3) + comps[i + 1]
						* (int) Math.pow(2, 2) + comps[i + 2]
						* (int) Math.pow(2, 1) + comps[i + 3];
				hashCode.append(ImageHelper.binaryToHex(result));
			}
		}

		return hashCode.toString();
	}

	/**
	 * 插入指纹
	 * 
	 * @param inputFile
	 *            输入文件
	 * @param outputFile
	 *            输出文件
	 * @param fingerInfo
	 *            指纹信息
	 * @return
	 */
	public static int insertFingerInfo(File inputFile, File outputFile,
			String fingerInfo) {
		try {
			InputStream is = new FileInputStream(inputFile);
			int bytes = is.available();
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(
					outputFile));
			byte[] b = new byte[bytes];
			int n = is.read(b, 0, bytes);
			is.close();
			dos.write(b, 0, n);
			dos.writeUTF(fingerInfo);
			dos.close();
			return bytes;
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
		return 0;
	}

	/**
	 * 验证指纹
	 * 
	 * 
	 * @param inputFile
	 *            输入文件
	 * @param n
	 *            指纹重数
	 * @return
	 */
	public static String readFingerInfo(File inputFile, int n) {

		try {
			InputStream inputStream = new FileInputStream(inputFile);
			DataInputStream dis = new DataInputStream(inputStream);
			long yyy = dis.skip(inputStream.available() - (18 + 16 * (n - 1)));
			String result = dis.readUTF();
			dis.close();
			return result;
		} catch (IOException e) {

		}
		return "";
	}

}