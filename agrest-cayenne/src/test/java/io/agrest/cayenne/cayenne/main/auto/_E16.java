package io.agrest.cayenne.cayenne.main.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.property.DateProperty;
import org.apache.cayenne.exp.property.PropertyFactory;

/**
 * Class _E16 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _E16 extends BaseDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String ID_PK_COLUMN = "id";

    public static final DateProperty<Date> C_DATE = PropertyFactory.createDate("cDate", Date.class);
    public static final DateProperty<Time> C_TIME = PropertyFactory.createDate("cTime", Time.class);
    public static final DateProperty<Timestamp> C_TIMESTAMP = PropertyFactory.createDate("cTimestamp", Timestamp.class);

    protected Date cDate;
    protected Time cTime;
    protected Timestamp cTimestamp;


    public void setCDate(Date cDate) {
        beforePropertyWrite("cDate", this.cDate, cDate);
        this.cDate = cDate;
    }

    public Date getCDate() {
        beforePropertyRead("cDate");
        return this.cDate;
    }

    public void setCTime(Time cTime) {
        beforePropertyWrite("cTime", this.cTime, cTime);
        this.cTime = cTime;
    }

    public Time getCTime() {
        beforePropertyRead("cTime");
        return this.cTime;
    }

    public void setCTimestamp(Timestamp cTimestamp) {
        beforePropertyWrite("cTimestamp", this.cTimestamp, cTimestamp);
        this.cTimestamp = cTimestamp;
    }

    public Timestamp getCTimestamp() {
        beforePropertyRead("cTimestamp");
        return this.cTimestamp;
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "cDate":
                return this.cDate;
            case "cTime":
                return this.cTime;
            case "cTimestamp":
                return this.cTimestamp;
            default:
                return super.readPropertyDirectly(propName);
        }
    }

    @Override
    public void writePropertyDirectly(String propName, Object val) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch (propName) {
            case "cDate":
                this.cDate = (Date)val;
                break;
            case "cTime":
                this.cTime = (Time)val;
                break;
            case "cTimestamp":
                this.cTimestamp = (Timestamp)val;
                break;
            default:
                super.writePropertyDirectly(propName, val);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeSerialized(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        readSerialized(in);
    }

    @Override
    protected void writeState(ObjectOutputStream out) throws IOException {
        super.writeState(out);
        out.writeObject(this.cDate);
        out.writeObject(this.cTime);
        out.writeObject(this.cTimestamp);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.cDate = (Date)in.readObject();
        this.cTime = (Time)in.readObject();
        this.cTimestamp = (Timestamp)in.readObject();
    }

}
