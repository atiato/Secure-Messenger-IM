
package future.im;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


public class Notif {

    public static int notifId = 654654;

    public static Notification getNotification(Context context) {
        Notification n;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, Login.class),
                0);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            n = new Notification(
                    R.drawable.stat_sample,
                    "Think Consult Chat",
                    System.currentTimeMillis());
            try {
                Method deprecatedMethod = n.getClass().getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
                deprecatedMethod.invoke(n, context, "Think & Consult IM", "Secure Communication...", pendingIntent);
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            // Use new API
            Notification.Builder builder = new Notification.Builder(context)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.stat_sample)
                    .setContentTitle("Think Consult Chat");
            n = builder.build();
        }

/*        Notification n = new Notification(
                R.drawable.stat_sample,
                "Think Consult Chat",
                System.currentTimeMillis());

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, Login.class),
                0);
        n.setLatestEventInfo(context, "Think & Consult IM", "Secure Communication...", pendingIntent);
*/
        return n;
    }

    public static void cancel(Context context) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.cancel(notifId);
    }

}
