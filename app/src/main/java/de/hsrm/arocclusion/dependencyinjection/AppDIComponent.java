package de.hsrm.arocclusion.dependencyinjection;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import de.hsrm.arocclusion.ARSceneRepository;
import de.hsrm.arocclusion.App;

@Component(modules = {AndroidSupportInjectionModule.class, AppDIModule.class})
public interface AppDIComponent extends AndroidInjector<App> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder arSceneRepository(ARSceneRepository arSceneRepository);

        AppDIComponent build();
    }
}
