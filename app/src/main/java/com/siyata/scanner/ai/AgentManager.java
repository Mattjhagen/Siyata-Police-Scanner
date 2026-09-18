package com.siyata.scanner.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages AI agents and their conversation states.
 * Handles agent registration, conversation history, and persistence.
 */
public class AgentManager {
    private static final String TAG = "AgentManager";
    private static final String PREFS_NAME = "ai_agents";
    private static final String KEY_AGENTS = "agents";
    private static final String KEY_CONVERSATIONS = "conversations";
    private static final int MAX_CONVERSATION_MESSAGES = 50; // Keep last 50 messages

    private static AgentManager instance;
    private Context context;
    private List<AIAgent> agents;
    private Map<String, List<ConversationMessage>> conversations; // agentId -> messages
    private SharedPreferences prefs;

    private AgentManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.agents = new ArrayList<>();
        this.conversations = new HashMap<>();

        loadAgents();
        loadConversations();

        // Initialize default agents if none exist
        if (agents.isEmpty()) {
            initializeDefaultAgents();
        }
    }

    public static synchronized AgentManager getInstance(Context context) {
        if (instance == null) {
            instance = new AgentManager(context);
        }
        return instance;
    }

    /**
     * Initialize the default AI agents
     */
    private void initializeDefaultAgents() {
        agents.add(new AIAgent(
            "General",
            "General-purpose conversational AI",
            "You are a helpful AI assistant. Provide clear, concise responses suitable for voice interaction. Keep responses under 200 words when possible.",
            AIAgent.AIProvider.OPENAI
        ));

        agents.add(new AIAgent(
            "Coding",
            "Software development assistant",
            "You are an expert software engineer. Help with coding questions, debugging, and architecture decisions. Prioritize practical, working solutions. Keep responses concise for voice interaction.",
            AIAgent.AIProvider.OPENAI
        ));

        agents.add(new AIAgent(
            "PacMac",
            "PacMac Mobile operations assistant",
            "You are a specialized assistant for PacMac Mobile operations, projects, and logistics. Provide operational guidance and project support. Keep responses brief and actionable.",
            AIAgent.AIProvider.OPENAI
        ));

        agents.add(new AIAgent(
            "Bible Study",
            "Bible study and scripture assistant",
            "You are a knowledgeable Bible study assistant. Help with scripture interpretation, theological questions, and spiritual guidance. Provide thoughtful, respectful responses suitable for voice interaction.",
            AIAgent.AIProvider.OPENAI
        ));

        agents.add(new AIAgent(
            "Home Lab",
            "Server, Linux, and networking assistant",
            "You are an expert in Linux systems administration, home lab setups, networking, and infrastructure. Help troubleshoot issues, suggest configurations, and explain technical concepts clearly for voice interaction.",
            AIAgent.AIProvider.OPENAI
        ));

        saveAgents();
        Log.d(TAG, "Initialized " + agents.size() + " default agents");
    }

    public List<AIAgent> getAgents() {
        return new ArrayList<>(agents);
    }

    public List<AIAgent> getEnabledAgents() {
        List<AIAgent> enabled = new ArrayList<>();
        for (AIAgent agent : agents) {
            if (agent.isEnabled()) {
                enabled.add(agent);
            }
        }
        return enabled;
    }

    public AIAgent getAgent(String agentId) {
        for (AIAgent agent : agents) {
            if (agent.getId().equals(agentId)) {
                return agent;
            }
        }
        return null;
    }

    public void addAgent(AIAgent agent) {
        agents.add(agent);
        saveAgents();
    }

    public void updateAgent(AIAgent agent) {
        for (int i = 0; i < agents.size(); i++) {
            if (agents.get(i).getId().equals(agent.getId())) {
                agents.set(i, agent);
                saveAgents();
                return;
            }
        }
    }

    public void deleteAgent(String agentId) {
        agents.removeIf(agent -> agent.getId().equals(agentId));
        conversations.remove(agentId);
        saveAgents();
        saveConversations();
    }

    /**
     * Get conversation history for an agent
     */
    public List<ConversationMessage> getConversation(String agentId) {
        List<ConversationMessage> messages = conversations.get(agentId);
        if (messages == null) {
            messages = new ArrayList<>();
            conversations.put(agentId, messages);
        }
        return new ArrayList<>(messages);
    }

    /**
     * Add a message to agent's conversation
     */
    public void addMessage(String agentId, ConversationMessage message) {
        List<ConversationMessage> messages = conversations.get(agentId);
        if (messages == null) {
            messages = new ArrayList<>();
            conversations.put(agentId, messages);
        }

        messages.add(message);

        // Trim old messages if exceeding limit (keep system prompt)
        while (messages.size() > MAX_CONVERSATION_MESSAGES) {
            // Remove oldest non-system message
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).getRole() != ConversationMessage.Role.SYSTEM) {
                    messages.remove(i);
                    break;
                }
            }
        }

        saveConversations();
    }

    /**
     * Clear conversation history for an agent
     */
    public void clearConversation(String agentId) {
        conversations.put(agentId, new ArrayList<>());
        saveConversations();
        Log.d(TAG, "Cleared conversation for agent: " + agentId);
    }

    /**
     * Get the last message from an agent conversation
     */
    public ConversationMessage getLastMessage(String agentId) {
        List<ConversationMessage> messages = conversations.get(agentId);
        if (messages != null && !messages.isEmpty()) {
            return messages.get(messages.size() - 1);
        }
        return null;
    }

    /**
     * Check if agent has active conversation
     */
    public boolean hasConversation(String agentId) {
        List<ConversationMessage> messages = conversations.get(agentId);
        return messages != null && !messages.isEmpty();
    }

    // Persistence methods
    private void saveAgents() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (AIAgent agent : agents) {
                JSONObject json = new JSONObject();
                json.put("id", agent.getId());
                json.put("name", agent.getName());
                json.put("description", agent.getDescription());
                json.put("systemPrompt", agent.getSystemPrompt());
                json.put("provider", agent.getProvider().name());
                json.put("model", agent.getModel());
                json.put("voice", agent.getVoice());
                json.put("enabled", agent.isEnabled());
                jsonArray.put(json);
            }
            prefs.edit().putString(KEY_AGENTS, jsonArray.toString()).apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error saving agents", e);
        }
    }

    private void loadAgents() {
        try {
            String json = prefs.getString(KEY_AGENTS, null);
            if (json != null) {
                JSONArray jsonArray = new JSONArray(json);
                agents.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    AIAgent agent = new AIAgent(
                        obj.getString("id"),
                        obj.getString("name"),
                        obj.getString("description"),
                        obj.getString("systemPrompt"),
                        AIAgent.AIProvider.valueOf(obj.getString("provider")),
                        obj.getString("model"),
                        obj.getString("voice"),
                        obj.getBoolean("enabled")
                    );
                    agents.add(agent);
                }
                Log.d(TAG, "Loaded " + agents.size() + " agents");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error loading agents", e);
        }
    }

    private void saveConversations() {
        try {
            JSONObject allConversations = new JSONObject();
            for (Map.Entry<String, List<ConversationMessage>> entry : conversations.entrySet()) {
                JSONArray messages = new JSONArray();
                for (ConversationMessage msg : entry.getValue()) {
                    JSONObject json = new JSONObject();
                    json.put("id", msg.getId());
                    json.put("agentId", msg.getAgentId());
                    json.put("role", msg.getRole().name());
                    json.put("content", msg.getContent());
                    json.put("timestamp", msg.getTimestamp().getTime());
                    json.put("type", msg.getType().name());
                    messages.put(json);
                }
                allConversations.put(entry.getKey(), messages);
            }
            prefs.edit().putString(KEY_CONVERSATIONS, allConversations.toString()).apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error saving conversations", e);
        }
    }

    private void loadConversations() {
        try {
            String json = prefs.getString(KEY_CONVERSATIONS, null);
            if (json != null) {
                JSONObject allConversations = new JSONObject(json);
                conversations.clear();

                for (java.util.Iterator<String> it = allConversations.keys(); it.hasNext(); ) {
                    String agentId = it.next();
                    JSONArray messages = allConversations.getJSONArray(agentId);
                    List<ConversationMessage> messageList = new ArrayList<>();

                    for (int i = 0; i < messages.length(); i++) {
                        JSONObject obj = messages.getJSONObject(i);
                        ConversationMessage msg = new ConversationMessage(
                            obj.getString("id"),
                            obj.getString("agentId"),
                            ConversationMessage.Role.valueOf(obj.getString("role")),
                            obj.getString("content"),
                            new Date(obj.getLong("timestamp")),
                            ConversationMessage.MessageType.valueOf(obj.getString("type"))
                        );
                        messageList.add(msg);
                    }
                    conversations.put(agentId, messageList);
                }
                Log.d(TAG, "Loaded conversations for " + conversations.size() + " agents");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error loading conversations", e);
        }
    }
}
