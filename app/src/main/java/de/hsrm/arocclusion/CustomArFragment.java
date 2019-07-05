package de.hsrm.arocclusion;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

public class CustomArFragment extends ArFragment {

    @Override
    protected Config getSessionConfiguration(Session session) {
        Config sessionConfiguration = super.getSessionConfiguration(session);
        sessionConfiguration.setFocusMode(Config.FocusMode.AUTO);
        return sessionConfiguration;
    }
}
