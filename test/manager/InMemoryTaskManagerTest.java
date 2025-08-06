package manager;

public class InMemoryTaskManagerTest extends ManagersTest {
    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }


}