package win.liyufan.im;

import java.net.InetSocketAddress;

/**
 * @author Rain
 * @date 2023/9/7 13:36
 */
public class ThreadLocalUtil {

    public static final ThreadLocal<InetSocketAddress> remoteAddress = ThreadLocal.withInitial(() -> null);

}
