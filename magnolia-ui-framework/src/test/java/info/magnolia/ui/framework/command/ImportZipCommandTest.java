package info.magnolia.ui.framework.command;

import static info.magnolia.test.hamcrest.NodeMatchers.hasNode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.context.Context;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.RepositoryException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link info.magnolia.ui.framework.command.ImportZipCommand}.
 */
public class ImportZipCommandTest extends MgnlTestCase {

    MockContext mockContext;
    private MockSession session;

    private ImportZipCommand importZipCommand;
    private ZipArchiveEntry zipArchiveEntry;
    private ZipFile zipFile;
    private SimpleTranslator translator;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockContext = MockUtil.getSystemMockContext();
        session = new MockSession("assets");
        MockUtil.getSystemMockContext().addSession("assets", session);
        translator = mock(SimpleTranslator.class);
        importZipCommand = new ImportZipCommandForHandleFileEntry(translator, mockContext);
        importZipCommand.setRepository("assets");
        zipFile = mock(ZipFile.class);
        zipArchiveEntry = mock(ZipArchiveEntry.class);

    }

    @Test
    public void testHandleFileEntry() throws IOException, RepositoryException {

        //GIVEN
        when(zipArchiveEntry.getName()).thenReturn("folderName/fileName.jpg");
        InputStream inputStream = new ByteArrayInputStream(new byte[10]);
        when(zipFile.getInputStream(zipArchiveEntry)).thenReturn(inputStream);

        //WHEN
        importZipCommand.handleFileEntry(zipFile, zipArchiveEntry); //then should not thrown PathNotFoundException

        //THEN
        assertThat(mockContext.getJCRSession("assets").getRootNode(), hasNode("folderName", NodeTypes.Folder.NAME));
    }

    private class ImportZipCommandForHandleFileEntry extends ImportZipCommand {

        public ImportZipCommandForHandleFileEntry(SimpleTranslator translator, Context context) {
            super(translator);
            this.context = context;
        }
    }
}
