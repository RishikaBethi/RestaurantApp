package utils.api;

public class APIUtils {

    public String addSpaces(String word)
    {
        if(word.contains("~"))
        {
            word = word.replaceAll("~"," ");
        }
        return word;
    }
}
