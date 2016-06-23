package com.csr.common;

import java.io.File;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestImageUtil extends TestCase {

	public void testMain() {
		
	}

	public void testImageCheck() {
		File destFile = new File("D:/pic/b_bak.jpg");
		File srcFile = new File("D:/pic/b.jpg");
        boolean result = ImageUtil.imageCheck(srcFile, destFile);
		Assert.assertEquals(ImageUtil.SRC_NOT_NULL, result); //ԪͼƬ����Ϊ��
		Assert.assertEquals(ImageUtil.ILLEGAL_INPUT, result); //�Ƿ�����
		Assert.assertEquals(ImageUtil.DEST_DISTORT, result);//��֤δͨ����ͼƬ���۸�
		
		
		
	}

}
