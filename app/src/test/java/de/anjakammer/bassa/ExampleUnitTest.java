package de.anjakammer.bassa;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import java.util.List;
import de.anjakammer.bassa.model.Answer;
import de.anjakammer.bassa.model.Question;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    private QuestionDataSource dataSource;
    private MainActivity main;
    private Question question;
    private Answer answer;
    private Answer answerTwo;

    @Before
    private void setUp() throws Exception{
        this.main = new MainActivity();
        this.dataSource = new QuestionDataSource(this.main);
        question = this.dataSource.createQuestion("first Question", "1");
        answer = this.dataSource.createAnswer("first Answer", "Peer1", question.getId());
        answerTwo = this.dataSource.createAnswer("first Answer", "Peer2", question.getId());
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
        this.dataSource.deleteQuestion(question);
    }
}