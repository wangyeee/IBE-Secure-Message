package hamaster.gradesgin.ibe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 一些常数
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public interface IBEConstraints {

	/**
	 * G*组元素的长度
	 */
	int IBE_G_SIZE = 128;

	/**
	 * Zr组元素的长度
	 */
	int IBE_ZR_SIZE = 20;

	/**
	 * 用户ID中使用的默认字符编码
	 */
	String USER_STRING_ENCODING = "UTF-8";

	/**
	 * 将实例持久化到输出流
	 * @param out 输出流
	 * @throws IOException 发生IO异常
	 */
	void writeExternal(OutputStream out) throws IOException;

	/**
	 * 从输入流中读取实例数据
	 * @param in 输入流
	 * @throws IOException 发生IO异常
	 * @throws ClassNotFoundException 读取的数据无法作为类的数据
	 */
	void readExternal(InputStream in) throws IOException, ClassNotFoundException;
}
