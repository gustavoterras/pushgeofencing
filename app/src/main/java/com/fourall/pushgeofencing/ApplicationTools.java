package com.fourall.pushgeofencing;

import android.app.Application;

import fourall.com.pay_lib.FourAllPayment;
import fourall.com.pay_lib.FourAll_FourAllURLS;
import fourall.com.pay_lib.PAYTYPE;

/**
 * Created by Gustavo Terras on 19/07/2017.
 */

public class ApplicationTools extends Application {

    //quando quer buildar em homolog com todos as bibliotecas em produção, nega p valor de debug
    private boolean buildType = BuildConfig.DEBUG;

    @Override
    public void onCreate() {
        super.onCreate();

        FourAllPayment.initLib(this);
        FourAllPayment.setApplicationId("ANDROID_APP4ALL_2.1.2");
        //Deve ser setado antes do servertype e após o setApplication
        //Deve ser os mesmos trackId da aplicação 4all
        FourAllPayment.setAnalyticsTrackingId(BuildConfig.DEBUG ? "UA-79356569-16" : "UA-79356569-17");
        FourAllPayment.setServerType(buildType ? FourAll_FourAllURLS.URLType.HOMOLOGATION : FourAll_FourAllURLS.URLType.PRODUCTION);
        FourAllPayment.setPaymentType(PAYTYPE.CREDIT);
        FourAllPayment.setNeedsCPF(false);

    }
}
