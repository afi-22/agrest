package io.agrest.cayenne.cayenne.main.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.property.NumericProperty;
import org.apache.cayenne.exp.property.PropertyFactory;

/**
 * Class _E29 was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _E29 extends BaseDataObject {

    private static final long serialVersionUID = 1L;

    public static final String ID1_PK_COLUMN = "id1";
    public static final String ID2_PK_COLUMN = "id2";

    public static final NumericProperty<Integer> ID2PROP = PropertyFactory.createNumeric("id2Prop", Integer.class);

    protected Integer id2Prop;


    public void setId2Prop(Integer id2Prop) {
        beforePropertyWrite("id2Prop", this.id2Prop, id2Prop);
        this.id2Prop = id2Prop;
    }

    public Integer getId2Prop() {
        beforePropertyRead("id2Prop");
        return this.id2Prop;
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "id2Prop":
                return this.id2Prop;
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
            case "id2Prop":
                this.id2Prop = (Integer)val;
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
        out.writeObject(this.id2Prop);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.id2Prop = (Integer)in.readObject();
    }

}
