package de.anjakammer.bassa;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import java.util.List;

import de.anjakammer.bassa.activities.MainActivity;
import de.anjakammer.bassa.models.Answer;
import de.anjakammer.bassa.models.Question;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    private ContentProvider contentProvider;
    private MainActivity main;
    private Question question;
    private Answer answer;
    private Answer answerTwo;

    @Before
    private void setUp() throws Exception{
        this.main = new MainActivity();
        this.contentProvider = new ContentProvider(this.main);
        question = this.contentProvider.createQuestion("first Question", "1");
        answer = this.contentProvider.createAnswer("first Answer", "Peer1", question.getId());
        answerTwo = this.contentProvider.createAnswer("first Answer", "Peer2", question.getId());
    }

    @Test
    public void getAnswerfromQuestion() throws Exception {
        setUp();
        List<Answer> answers = question.getAnswers();
        String answerString = answers.get(1).toString();
        Assert.assertSame(answerString,("Peer1: first Answer"));
//        Assert.assertSame("möp","möp");
        tearDown();
    }

    @Test
    public void getAnswerfromList() throws Exception {
        setUp();
        List<Answer> answers = question.getAnswers();
        Assert.assertTrue(answers.contains(answer));
        tearDown();
    }

    @After
    private void tearDown() throws Exception{
        this.contentProvider.deleteQuestion(question);
    }
}