package blinking;

import blinking.config.Config;
import blinking.run.Step1;
import blinking.run.Step2;
import blinking.run.Step3;
import blinking.run.Step4;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        Config.clear();
        Config.init();
        Step1.main(new String[]{});
        Step2.main(new String[]{});
        Step3.main(new String[]{});
        Step4.main(new String[]{});
    }
}
