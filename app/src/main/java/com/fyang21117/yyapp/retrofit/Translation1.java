package com.fyang21117.yyapp.retrofit;

import java.util.List;
public class Translation1 {

    private String type;//翻译类型
    private int errorCode;//0表示成功
    private int elapsedTime;
    private List<List<TranslateResultBean>> translateResult;

    public List<List<TranslateResultBean>> getTranslateResult() {
        return translateResult;
    }

    public static class TranslateResultBean {
        /**
         * src : 原文
         * tgt : 译文
         */

        public String src;
        public String tgt;

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public String getTgt() {
            return tgt;
        }

        public void setTgt(String tgt) {
            this.tgt = tgt;
        }
    }

}




