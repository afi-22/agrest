package io.agrest.cayenne.cayenne.main.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.property.EntityProperty;
import org.apache.cayenne.exp.property.PropertyFactory;
import org.apache.cayenne.exp.property.StringProperty;

import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E5;

/**
 * Class _E3 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _E3 extends BaseDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String ID__PK_COLUMN = "id_";

    public static final StringProperty<String> NAME = PropertyFactory.createString("name", String.class);
    public static final StringProperty<String> PHONE_NUMBER = PropertyFactory.createString("phoneNumber", String.class);
    public static final EntityProperty<E2> E2 = PropertyFactory.createEntity("e2", E2.class);
    public static final EntityProperty<E5> E5 = PropertyFactory.createEntity("e5", E5.class);

    protected String name;
    protected String phoneNumber;

    protected Object e2;
    protected Object e5;

    public void setName(String name) {
        beforePropertyWrite("name", this.name, name);
        this.name = name;
    }

    public String getName() {
        beforePropertyRead("name");
        return this.name;
    }

    public void setPhoneNumber(String phoneNumber) {
        beforePropertyWrite("phoneNumber", this.phoneNumber, phoneNumber);
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        beforePropertyRead("phoneNumber");
        return this.phoneNumber;
    }

    public void setE2(E2 e2) {
        setToOneTarget("e2", e2, true);
    }

    public E2 getE2() {
        return (E2)readProperty("e2");
    }

    public void setE5(E5 e5) {
        setToOneTarget("e5", e5, true);
    }

    public E5 getE5() {
        return (E5)readProperty("e5");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "name":
                return this.name;
            case "phoneNumber":
                return this.phoneNumber;
            case "e2":
                return this.e2;
            case "e5":
                return this.e5;
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
            case "name":
                this.name = (String)val;
                break;
            case "phoneNumber":
                this.phoneNumber = (String)val;
                break;
            case "e2":
                this.e2 = val;
                break;
            case "e5":
                this.e5 = val;
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
        out.writeObject(this.name);
        out.writeObject(this.phoneNumber);
        out.writeObject(this.e2);
        out.writeObject(this.e5);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.name = (String)in.readObject();
        this.phoneNumber = (String)in.readObject();
        this.e2 = in.readObject();
        this.e5 = in.readObject();
    }

}
