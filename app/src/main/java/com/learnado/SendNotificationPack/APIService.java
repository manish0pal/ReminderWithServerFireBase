package com.learnado.SendNotificationPack;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAmypU3hU:APA91bFybeup9E-tgZPXhwGCmGm7GlNDzZOTb2VCBQC6f2w5KYS1mhePAdhBl-FzpyyHPFV06kmI3Di3_4wKq-AUJSk3g7jDUWu-0Ck63Mex0kk1HXd2AJtJZ8rzTXKz0Q_AVRyWOGS0" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}

