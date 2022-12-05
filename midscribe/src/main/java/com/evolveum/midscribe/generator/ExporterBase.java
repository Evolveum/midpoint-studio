package com.evolveum.midscribe.generator;

import org.asciidoctor.Asciidoctor;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class ExporterBase implements Exporter {

    private LogListener listener;

    @Override
    public void setLogListener(LogListener listener) {
        this.listener = listener;
    }

    protected Asciidoctor createAsciidoctor() {
        // this should improve performance of JRuby
        System.setProperty("jruby.compat.version", "RUBY1_9");
        System.setProperty("jruby.compile.mode", "OFF");

        Asciidoctor doctor = Asciidoctor.Factory.create();
        if (listener != null) {
            doctor.registerLogHandler(new MidscribeLogHandler(listener));
        }

        return doctor;
    }
}
