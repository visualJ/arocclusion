package de.hsrm.arocclusion;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import de.hsrm.arocclusion.dependencyinjection.DaggerAppDIComponent;

public class App extends DaggerApplication {

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppDIComponent.builder()
                .arSceneRepository(new ARSceneRepository(this))
                .build();
    }

}
