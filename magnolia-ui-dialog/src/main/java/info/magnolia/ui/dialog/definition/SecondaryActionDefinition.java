package info.magnolia.ui.dialog.definition;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/4/13
 * Time: 11:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class SecondaryActionDefinition {

    public SecondaryActionDefinition() {
    }

    public SecondaryActionDefinition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof SecondaryActionDefinition) {
            SecondaryActionDefinition that = (SecondaryActionDefinition) o;
            return name == null ? that.name == null : name.equals(that.name);
        }

        if (o instanceof String) {
            String oName = (String)o;
            return name == null ? oName == null : name.equals(oName);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
