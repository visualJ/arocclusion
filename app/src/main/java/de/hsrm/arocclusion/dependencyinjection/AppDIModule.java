package de.hsrm.arocclusion.dependencyinjection;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import de.hsrm.arocclusion.MainActivity;

@Module
public abstract class AppDIModule {

    @ContributesAndroidInjector
    abstract MainActivity mainActivity();


}
