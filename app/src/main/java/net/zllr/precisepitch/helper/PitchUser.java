package net.zllr.precisepitch.helper;


/**
 * This class describe the user model, all user have a DataHisto associated
 */
public class PitchUser {

    private String username;
    private DataHisto dataHisto;
    private enum userLevel {
        BEGINNER,
        INTERMEDIATE,
        EXPERT
    }

    /**
     * Create user with the constructor, datahisto is generated
     * @param username
     */
    public PitchUser(String username) {
        this.username = username;
        dataHisto = new DataHisto();
    }

    public String getUsername() {
        return username;
    }

    public DataHisto getDataHisto() {
        return dataHisto;
    }
}
