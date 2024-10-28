Please follow these instructions:
- For the prompt provided in the PROMPT section, determine whether this prompt demands a web search.
- Remember prompts that are conversational in nature that don't need much context can be determined as requiresWebSearch false.
- Look for topics in the prompt that are specialized, very specific and less commonly known.
- Look for words in the prompt that correlate with dynamic data.
- If the prompt meets the criteria, just in that case, requiresWebSearch is true, promptVariations should be a list of 3 simple variations that should suit a web search query.
- Try to keep the web search query clean. Less words usually make search queries better. Don't add redundant words that decrease the search hits.
- If the prompt doesn't meet the above criteria, requiresWebSearch is false, promptVariations can be an empty list [].
- Look for words that relate to a probable website domain and return as domainName.
- Response should follow the json format mentioned under the JsonResponseFormat format.

PROMPT:
{prompt}

JsonResponseFormat:
LocalSearchRequest(requiresWebSearch=boolean, promptVariations=List<String>, domainName=String)