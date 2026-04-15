package com.vaul.vaul.enums.branches;

public enum ExistsBranches {
    TIRTHAPURI(4312),
    AMBAD(2352),
    GHANSAWANGI(2789),
    WADIGODRI(3407),
    PARTUR(2002),
    SHAHAGADH(2037),
    RANJANI(2715),
    KUMBHAR_PIMPALGAON(2782),
    JALNA_MAIN(7150),
    KHANDALA_VAUL(3256),
    BADNAPUR(2614);

    private final int branchCode;

    ExistsBranches(int branchCode) {
        this.branchCode = branchCode;
    }

    public int getBranchCode() {
        return branchCode;
    }
}

