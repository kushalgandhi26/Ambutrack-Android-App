package com.example.ambutrack.Remote;

import com.example.ambutrack.ui.FCMResponse;
import com.example.ambutrack.ui.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAc6d6mSY:APA91bHIBnumbaiOlzBx-yVi5bTl4WsCiWtMJK8PVVQ8rn-2LRL5-i8_D8DsWj95_5lfn7j9tNXjWaX5Poghj1MmDJIlWXyJGJLm3jrZdFeAyfmGc03ziekI5-XZUaDTDrsnUnnFDJv3"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
