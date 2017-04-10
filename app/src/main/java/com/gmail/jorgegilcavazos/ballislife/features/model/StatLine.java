package com.gmail.jorgegilcavazos.ballislife.features.model;

public class StatLine {

    private int pts;
    private int min;
    private int ast;
    private int reb;
    private int blk;
    private int stl;
    private int tov;
    private int pf;
    private int fga;
    private int fgm;
    private String fn;
    private String ln;

    public StatLine(int pts, int min, int ast, int reb, int blk, int stl, int tov, int pf, int fga, int fgm, String fn, String ln) {
        this.pts = pts;
        this.min = min;
        this.ast = ast;
        this.reb = reb;
        this.blk = blk;
        this.stl = stl;
        this.tov = tov;
        this.pf = pf;
        this.fga = fga;
        this.fgm = fgm;
        this.fn = fn;
        this.ln = ln;
    }

    public int getPts() {
        return pts;
    }

    public void setPts(int pts) {
        this.pts = pts;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getAst() {
        return ast;
    }

    public void setAst(int ast) {
        this.ast = ast;
    }

    public int getReb() {
        return reb;
    }

    public void setReb(int reb) {
        this.reb = reb;
    }

    public int getBlk() {
        return blk;
    }

    public void setBlk(int blk) {
        this.blk = blk;
    }

    public int getStl() {
        return stl;
    }

    public void setStl(int stl) {
        this.stl = stl;
    }

    public int getTov() {
        return tov;
    }

    public void setTov(int tov) {
        this.tov = tov;
    }

    public int getPf() {
        return pf;
    }

    public void setPf(int pf) {
        this.pf = pf;
    }

    public int getFga() {
        return fga;
    }

    public void setFga(int fga) {
        this.fga = fga;
    }

    public int getFgm() {
        return fgm;
    }

    public void setFgm(int fgm) {
        this.fgm = fgm;
    }

    public String getFn() {
        return fn;
    }

    public void setFn(String fn) {
        this.fn = fn;
    }

    public String getLn() {
        return ln;
    }

    public void setLn(String ln) {
        this.ln = ln;
    }
}
