package blinking;

import blinking.config.Config;
import blinking.run.*;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        Config.clear();
        Config.init();
        Step1.main(new String[]{});
        Step2.main(new String[]{});
        Step3.main(new String[]{});
        Step4.main(new String[]{});
        Step5.main(new String[]{});
    }
}
