package br.com.requestmngr.user;

public record LocalUser(Long id, String username, String passwordHash, String displayName, UserRole role, boolean active) { }
